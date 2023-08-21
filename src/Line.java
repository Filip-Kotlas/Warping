import java.awt.*;

public class Line
{
    public Point p1;
    public Point p2;
    Color color;


    public Line()
    {
        setFirstPoint( new Point( 0, 0 ) );
        setSecondPoint(new Point( 0, 0 ) );
        color = Color.RED;
    }

    public Line( Point p )
    {
        setFirstPoint(p);
        setSecondPoint(new Point( 0, 0 ) );
        color = Color.RED;
    }

    public Line( Point p1, Point p2 )
    {
        setFirstPoint(p1);
        setSecondPoint(p2);
        color = Color.RED;
    }

    public Line( Line source )
    {
        setFirstPoint( new Point( source.p1 ) );
        setSecondPoint( new Point( source.p2 ) );
        color = Color.RED;
    }

    // Vrátí true, pokud jsou dva body v menší vzdálenosti než určená vzdálenost.
    public static boolean areClose( Point p1, Point p2, double distance )
    {
        return Math.abs( p1.x - p2.x ) < distance && Math.abs( p1.y - p2.y ) < distance;
    }

    public void setFirstPoint( Point p )
    {
        this.p1 = p;
    }

    public void setSecondPoint( Point p )
    {
        this.p2 = p;
    }

    public void setColor( Color color )
    {
        this.color = color;
    }

    // Vykreslí úsečku s šipkou v koncovém bodu a krátkou kolmicí v počátečním bodu.
    // start_point určuje počátek obrázku v g
    // ratio určuje jak se mění škálování souřadnic
    public void repaint( Graphics2D g, Point start_point, double ratio )
    {
        float stroke_width = 3f;
        g.setStroke( new BasicStroke(stroke_width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g.setColor( this.color );

        int p1x = (int) (p1.x * ratio );
        int p1y = (int) (p1.y * ratio );
        int p2x = (int) (p2.x * ratio );
        int p2y = (int) (p2.y * ratio );
        int decor_size = 5;
        
        int per_vect_x = ( p1y - p2y ); // Math.sqrt( pow( p1y - p2y, 2 ) + pow( p2x - )));
        int per_vect_y = ( p2x - p1x ); //;
        double per_vect_size = Math.sqrt( Math.pow( per_vect_x, 2 ) + Math.pow( per_vect_y, 2 ) );
        per_vect_x /= per_vect_size / decor_size;
        per_vect_y /= per_vect_size / decor_size;

        int parall_vect_x = ( p2x - p1x );
        int parall_vect_y = ( p2y - p1y );
        double parall_vect_size = Math.sqrt( Math.pow( parall_vect_x, 2 ) + Math.pow( parall_vect_y, 2 ) );
        parall_vect_x /= parall_vect_size / decor_size;
        parall_vect_y /= parall_vect_size / decor_size;

        int arrow_point1_x = per_vect_x - parall_vect_x;
        int arrow_point1_y = per_vect_y - parall_vect_y;
        int arrow_point2_x = -per_vect_x - parall_vect_x;
        int arrow_point2_y = -per_vect_y - parall_vect_y;

        g.drawLine( start_point.x + p1x + per_vect_x, start_point.y + p1y + per_vect_y, start_point.x + p1x - per_vect_x, start_point.y + p1y - per_vect_y );
        g.drawLine( start_point.x + p1x, start_point.y + p1y, start_point.x + p2x, start_point.y + p2y );
        g.drawLine( start_point.x + p2x + arrow_point1_x, start_point.y + p2y + arrow_point1_y, start_point.x + p2x, start_point.y + p2y );
        g.drawLine( start_point.x + p2x + arrow_point2_x, start_point.y + p2y + arrow_point2_y, start_point.x + p2x, start_point.y + p2y );
    }

    // Vrací délku úsečky.
    public float length()
    {
        return (float) Math.sqrt( Math.pow( p1.x - p2.x, 2 ) + Math.pow( p1.y - p2.y, 2 ) );
    }
}
