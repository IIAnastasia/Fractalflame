package flame.objects;

public class Pixel {
    Color color = new Color(0, 0, 0);
    int hitCount;

    public Color getColor() {
        return color;
    }

    public void setColor(Color another) {
        this.color = another;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public void addCount(int count) {
        hitCount += count;
    }

    public void addCount() {
        ++this.hitCount;
    }
}
