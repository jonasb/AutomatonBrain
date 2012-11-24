package se.forskningsavd.automatonbrain;

class Navigator {
    public boolean left = false;
    public boolean right = false;
    public boolean up = false;
    public boolean down = false;

    public float moveX = 0; // -1.0..1.0
    public float moveY = 0; // -1.0..1.0
    public float rotation = 0; // -1.0..1.0
    public float cameraAngle = 0; // -1.0..1.0

    public void reset() {
        up = down = left = right = false;
        moveX = moveY = rotation = cameraAngle = 0;
    }
}
