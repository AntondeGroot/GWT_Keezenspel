package adg.keezen.util;

import adg.keezen.Point;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

public class PawnLayout {

  public static final int WIDTH = 50;
  public static final int HEIGHT = 50;

  private PawnLayout() {
  }

  public static double getLeft(Point point) {
    return point.getX() - WIDTH / 2.0;
  }

  public static double getTop(Point point) {
    return point.getY() - HEIGHT / 2.0 - 15;
  }

  public static void applyPosition(Element element, Point point) {
    element.getStyle().setLeft(getLeft(point), Style.Unit.PX);
    element.getStyle().setTop(getTop(point), Style.Unit.PX);
    element.getStyle().setZIndex((int) point.getY());
  }

  /** Returns [left, top, width, height] — suitable for canvas drawImage calls. */
  public static double[] getRect(Point point) {
    return new double[] {getLeft(point), getTop(point), WIDTH, HEIGHT};
  }
}