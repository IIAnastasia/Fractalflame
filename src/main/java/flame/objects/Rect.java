package flame.objects;

import java.util.Iterator;

public record Rect(int x, int y, int width, int height) {
    public boolean contains(Point p) {
        return p.x() >= x && p.x() < x + width && p.y() >= y && p.y() < y + height;
    }

    public RectIterator iterator() {
        return new RectIterator(this);
    }

    public class RectIterator implements Iterator<Point> {
        Rect rect;
        private int currentX;
        private int currentY;

        public RectIterator(Rect rect) {
            this.rect = rect;
        }

        @Override public boolean hasNext() {
            return (currentY < rect.height())
                && (currentX < rect.width());
        }

        @Override public Point next() {
            Point answer = null;
            if (hasNext()) {
                answer = new Point(currentX + rect.x(), currentY + rect.y());
                ++currentX;
                if (currentX >= rect.width()) {
                    currentX = 0;
                    ++currentY;
                }

            }
            return answer;
        }
    }

}
