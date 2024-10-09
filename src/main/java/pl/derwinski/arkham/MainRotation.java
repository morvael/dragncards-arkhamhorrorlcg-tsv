package pl.derwinski.arkham;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import static pl.derwinski.arkham.Util.log;

/**
 *
 * @author morvael
 */
public class MainRotation {

    private void print(String name, AffineTransform a, Point2D.Double p, int angle) {
        Point2D pp = a.transform(p, null);
        System.out.println(String.format("                        [\"DO_SET_CARD_LOCATION_AND_ROTATION\", \"$%s\", %d, %d, %d]%s", name, Math.round(pp.getX()), Math.round(pp.getY()), angle, "LEGSE".equals(name) ? "" : ","));
    }

    private void generate(int angle) {
        Point2D.Double center = new Point2D.Double(0d, 0d);
        Point2D.Double nw = new Point2D.Double(-14d, -11d);
        Point2D.Double ne = new Point2D.Double(14d, -11d);
        Point2D.Double se = new Point2D.Double(14d, 11d);
        Point2D.Double sw = new Point2D.Double(-14d, 11d);

        AffineTransform a = AffineTransform.getTranslateInstance(45, 43);
        a.rotate(Math.toRadians(angle));

        System.out.println("angle " + angle);
        print("TORSO", a, center, angle);
        print("LEGNW", a, nw, angle);
        print("LEGNE", a, ne, angle);
        print("LEGSW", a, se, angle);
        print("LEGSE", a, sw, angle);
        System.out.println();
    }

    public void run() throws Exception {
        for (int i = 0; i < 8; i++) {
            generate(i * 45);
        }
    }

    public static void main(String[] args) {
        try {
            new MainRotation().run();
        } catch (Exception ex) {
            log(ex);
        }
    }

}
