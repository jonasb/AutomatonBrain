#include <jni.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <stdbool.h>
#include <android/bitmap.h>

AVCodecContext    *pCodecCtx;                  // FFMPEG codec context
AVCodec           *pCodec;                     // Pointer to FFMPEG codec (H264)
AVFrame           *pFrame;                     // Used in the decoding process
struct SwsContext *convertCtx;                 // Used in the scaling/conversion process
AVPacket           avpkt;                      // Used in the decoding process
int                temp;                       // Various uses


bool Java_se_forskningsavd_Decoder_init(JNIEnv* env, jobject thiz) {
    avcodec_init();
    avcodec_register_all();
    pCodecCtx = avcodec_alloc_context();
    pCodec = avcodec_find_decoder( CODEC_ID_H264 );
    av_init_packet( &avpkt );
    if( !pCodec ) {
        return false;
        //printf( "RoboCortex [error]: Unable to initialize decoder\n" );
        //exit( EXIT_DECODER );
    }
    avcodec_open( pCodecCtx, pCodec );

    // Allocate decoder frame
    pFrame = avcodec_alloc_frame();
    return true;
}

bool Java_se_forskningsavd_Decoder_decode(JNIEnv *env, jobject thiz, jbyteArray frame, jobject bitmap) {
    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != 0) {
        return false;
    }

    uint8_t *dest_data = 0; //TODO
    avpkt.data = (*env)->GetByteArrayElements(env, frame, 0);
    avpkt.size = (*env)->GetArrayLength(env, frame);
    avpkt.flags = AV_PKT_FLAG_KEY;
    int len = avcodec_decode_video2( pCodecCtx, pFrame, &temp, &avpkt );
    (*env)->ReleaseByteArrayElements(env, frame, avpkt.data, JNI_ABORT);

    if (len < 0 ) {
        return false;
        //printf( "RoboCortex [info]: Decoding error (packet loss)\n" );
    } else {
        void *bitmapData;
        AndroidBitmap_lockPixels(env, bitmap, &bitmapData);
        const uint8_t * data[1] = { bitmapData };
        int linesize[1] = { bitmapInfo.stride };

        // Create scaling & color-space conversion context
        convertCtx = sws_getContext( pCodecCtx->width, pCodecCtx->height, pCodecCtx->pix_fmt,
            bitmapInfo.width, bitmapInfo.height, PIX_FMT_BGRA, SWS_AREA, NULL, NULL, NULL);

        // Scale and convert the frame
        sws_scale( convertCtx, (const uint8_t**) pFrame->data, pFrame->linesize, 0,
        pCodecCtx->height, (uint8_t * const*) data, linesize );

        // Cleanup
        sws_freeContext( convertCtx );

        //
        AndroidBitmap_unlockPixels(env, bitmap);
    }
    return true;
}

