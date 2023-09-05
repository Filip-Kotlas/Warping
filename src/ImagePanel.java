import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

class ImagePanel extends JPanel
{
    public BufferedImage image; // samotný obrázek
    public int[][] image_data;     // data z obrázku
    public int image_width;     // šířka obrázku v paměti
    public int image_height;    // výška obrázku v paměti
    public ArrayList<Line> lines = new ArrayList<>();   // list úseček
    public ImagePanel other_panel;  // odkaz na druhý panel
    public boolean lines_visible;   // flag pro viditelnost úseček
    public boolean drawing_line = false;    // flag pro kreslení úsečky
    public boolean editing_line = false;    // flag pro editaci úsečky
    public int edited_line_index;   // index právě editované úsečky
    public int edited_point_index;  // index právě editovaného bodu editované úsečky
    public Point image_beginning = new Point( 0, 0 );   // souřadnice levého horního rohu obrázku
    public Dimension drawn_image_dim = new Dimension( 0, 0 ); // rozměry orbázku na obrazovce
    private long color_seed = 0;    // seed pro náhodné generování barev úseček



    public ImagePanel()
    {
        this.image_data = null;
        this.lines.clear();
        lines_visible = true;
    }

    public ImagePanel( BufferedImage image )
    {
        loadImage(image);
    }

    public void addSecondPanel( ImagePanel secondPanel )
    {
        other_panel = secondPanel;
    }

    public void loadImage( BufferedImage _image )
    {
        image = _image;
        image_width = _image.getWidth();
        image_height = _image.getHeight();
        image_data = new int[image_width][image_height];
        for( int i = 0; i < image_width; i++ )
        {
            for( int j = 0; j < image_height; j++ )
            {
                image_data[i][j] = _image.getRGB(i, j);
            }
        }
        this.lines.clear();
        lines_visible = true;
    }

    // Vykresluje tento panel a jeho obsah.
    @Override
    protected void paintComponent(Graphics g)
    {
        if( this.image_data != null )
        {
            setBackground( UIManager.getColor("InternalFrame.background") );
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int panel_width = getWidth();
            int panel_height = getHeight();
            double panel_ratio = (double) panel_width / panel_height;
            double image_ratio = (double) image_width / image_height;

            // V závislosti na aktuálních rozměrech panelu určuje rozměry a umístění obrázku v panelu.
            // Umísťuje obrázek doprostřed panelu v největší možné velikosti.
            if( panel_ratio <= image_ratio )
            {
                image_beginning.x = 0;
                image_beginning.y = ( panel_height - (int) Math.floor( panel_width / image_ratio ) ) / 2;
                drawn_image_dim.width = panel_width;
                drawn_image_dim.height = (int) Math.floor( panel_width / image_ratio );
            }
            else
            {
                image_beginning.x = ( panel_width - (int) Math.floor( panel_height * image_ratio ) ) / 2;
                image_beginning.y = 0;
                drawn_image_dim.width = (int) Math.floor( panel_height * image_ratio );
                drawn_image_dim.height = panel_height;
            }

            g2d.drawImage( image, image_beginning.x, image_beginning.y, drawn_image_dim.width, drawn_image_dim.height, null );

            // Vykreslení úseček.
            if( lines_visible ) 
            {
                for( Line line : lines )
                {
                    line.repaint( g2d, image_beginning, drawn_image_dim.width / (double) this.image_width );
                }
            }

            g2d.dispose();
        }
        else
        {
            setBackground( Color.LIGHT_GRAY );
            super.paintComponent(g);
        }
    }

    private void updateImage()
    {
        for( int i = 0; i < image_width; i++ )
        {
            for( int j = 0; j < image_height; j++ )
            {
                image.setRGB(i, j, image_data[i][j]);
            }
        }
    }

    // Začne kreslit úsečku.
    public void startLine( Point mouse_pos, boolean original )
    {
        Point point = new Point( convert_to_image_size( mouse_pos ) );

        // Nastavení barvy úsečky
        Random random = new Random( this.color_seed );
        this.color_seed += 5000;
        final float hue = random.nextFloat();
        final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
        Color color = Color.getHSBColor(hue, saturation, luminance);

        // Přidání nové úsečky.
        lines.add( new Line() );
        lines.get( lines.size() - 1 ).setFirstPoint( point );
        lines.get( lines.size() - 1 ).setSecondPoint( point );
        lines.get( lines.size() - 1 ).setColor( color );
        drawing_line = true;

        // Provede to samé pro druhý panel.
        if( original )
        {
            other_panel.startLine( mouse_pos, false );
        }
    }

    // Aktualizace kreslené/editované úsečky
    public void updateDrawnLine( Point mouse_pos, boolean original )
    {
        if( drawing_line )
        {
            Point point = convert_to_image_size( mouse_pos );   // Přechod do souřadnic spojených s obrázkem.
            lines.get( lines.size() - 1 ).setSecondPoint( point );
            
            // Provede to samé pro druhý panel.
            if( original )
            {
                other_panel.updateDrawnLine( mouse_pos, false );
            }
        }
        if( editing_line )
        {
            Point point = convert_to_image_size( mouse_pos ); // Přechod do souřadnic spojených s obrázkem. 

            // Aktualizuje odpovídající bod odpovídající úsečky.
            if( edited_point_index == 1 )
            {
                lines.get( edited_line_index ).setFirstPoint( point );
            }
            else if( edited_point_index == 2 )
            {
                lines.get( edited_line_index ).setSecondPoint( point );
            }
        }
        repaint();
    }

    // Vrátí true, pokud je myš uvnitř obrázku.
    public boolean isMouseOutOfBounds ( Point mouse_position )
    {
        return  mouse_position.x < image_beginning.x ||
                mouse_position.x > image_beginning.x + drawn_image_dim.width ||
                mouse_position.y < image_beginning.y ||
                mouse_position.y > image_beginning.y + drawn_image_dim.height;
       
    }

    // Pokud byla myš v dostatečné blízkosti k nějakému bodu, uchopí tento bod a tím ho začne editovat.
    // Pokud je v dosahu více bodů, uchopí ten, co je nejblíž.
    public boolean fetchPoint( Point mouse_pos )
    {
        int trigger_distance = 10;
        Point mouse_position = this.convert_to_image_size( mouse_pos );
        edited_line_index = -1;
        edited_point_index = -1;
        float min_distance = (float) ( trigger_distance / drawn_image_dim.getWidth() * this.image_width );

        // Prochází všechny existující úsečky.
        for( int i = 0; i < lines.size(); i++ )
        {
            Line line = lines.get(i);

            // Kontroluje počáteční bod úsečky.
            if( Line.areClose( line.p1, mouse_position, trigger_distance / drawn_image_dim.getWidth() * this.image_width ) )
            {
                double distance = new Line( line.p1, mouse_position ).length();
                if( distance < min_distance )
                {
                    min_distance = (float) distance;
                    editing_line = true;
                    edited_line_index = i;
                    edited_point_index = 1;
                }
            }

            // Kontroluje konečný bod úsečky.
            if( Line.areClose( line.p2, mouse_position, trigger_distance / drawn_image_dim.getWidth() * this.image_width ) )
            {
                double distance = new Line( line.p2, mouse_position ).length();
                if( distance < min_distance )
                {
                    min_distance = (float) distance;
                    editing_line = true;
                    edited_line_index = i;
                    edited_point_index = 2;
                }
            }
        }
        return editing_line;
    }

    // Přepočítá souřadnice bodu z obrazovky do souřadnic daných reálným rozlišením obrázku.
    private Point convert_to_image_size( Point screen_point )
    {
        Point image_point = new Point();
        image_point.x = (int) ( ( screen_point.x - image_beginning.x ) / this.drawn_image_dim.getWidth() * this.image_width );
        image_point.y = (int) ( ( screen_point.y - image_beginning.y ) / this.drawn_image_dim.getWidth() * this.image_width );
        return image_point;
    }

    // Uvolní právě kreslenou/editovanou úsečku.
    public void release_line( boolean original )
    {
        if( drawing_line || editing_line && !lines.isEmpty() )
        {
            drawing_line = false;
            editing_line = false;

            // Pokud je výsledkem deformovaná úsečka, tj. oba body se shodují, úsečku vymaže.
            if( lines.get( lines.size() - 1 ).p1.equals( lines.get( lines.size() - 1 ).p2 ) )
            {
                this.deleteLineByIndex( lines.size() - 1 );
            }

            // Provede to samé pro druhý obrázek.
            if( original )
            {
                other_panel.release_line( false );
            }
        }
    }

    // Pokud se nějaký bod nějaké úsečky nachází v dostatečné blízkosti myši při kliknutí, vymaže tuto úsečku.
    // Pokud je takových bodů více, vymaže tu úsečku, která je nejblíž.
    public void delete_line( Point mouse_pos )
    {
        int index_of_line_to_delete = -1;
        float trigger_distance = 10;
        float min_distance = (float) (trigger_distance / drawn_image_dim.getWidth() * this.image_width ); // Přepočítání do souřadnic obrázku.
        Point mouse_position = this.convert_to_image_size( mouse_pos ); // Přepočítání do souřadnic spojených s obrázkem.

        // Prochází všechny úsečky.
        for( int i = 0; i < lines.size(); i++ )
        {
            Line line = lines.get(i);

            // Kontroluje vzdálenost obou krajních bodů k myši.
            if( Line.areClose( line.p1, mouse_position, trigger_distance / drawn_image_dim.getWidth() * this.image_width ) ||
                Line.areClose( line.p2, mouse_position, trigger_distance / drawn_image_dim.getWidth() * this.image_width ) )
            {
                // Ukládá index úsečky k vymazání, pokud se jedná o zatím nejbližší úsečku.
                float distance1 = new Line( line.p1, convert_to_image_size(mouse_pos) ).length();
                float distance2 = new Line( line.p2, convert_to_image_size(mouse_pos) ).length();
                if( distance1 < min_distance || distance2 < min_distance )
                {
                    min_distance = Math.min( distance1, distance2 );
                    index_of_line_to_delete = i;
                }
            }
        }

        // Samotné mazání nejbližší úsečky.
        if( index_of_line_to_delete >= 0 )
        {
            this.deleteLineByIndex( index_of_line_to_delete );
            this.other_panel.deleteLineByIndex( index_of_line_to_delete );
        }
    }

    // Provádí přímo mazání podle indexu.
    public void deleteLineByIndex( int i )
    {
        lines.remove(i);
        this.repaint();
    }

    // Provede warping tohoto obrázku.
    public void warp(float a, float b, float p, float integration_step, boolean use_bilinear, boolean use_antialiasing )
    {
        // Vypíše zprávu, že nebyl načten obrázek.
        if( this.image_data == null )
        {
            JOptionPane.showMessageDialog(null, "Nejprve načtěte obrázek.", "Chybí obrázek.", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        if( use_antialiasing )
        {
            FloatPoint[][] X_prime_field = new FloatPoint[image_width + 1][image_height + 1];
            for( int i = 0; i <= this.image_width; i++ )
            {
                for( int j = 0; j <= this.image_height; j++ )
                {
                    X_prime_field[i][j] = computeXPrime(new FloatPoint( i - 0.5f, j - 0.5f ), a, b, p);
                }
            }

            for( int i = 0; i < this.image_width; i++ )
            {
                for( int j = 0; j < this.image_height; j++ )
                {
                    FloatPoint[] verticies = {  X_prime_field[ i ][ j ],
                                                X_prime_field[ i ][ j + 1 ],
                                                X_prime_field[ i + 1 ][ j + 1 ],
                                                X_prime_field[ i + 1 ][ j ] };
                    boolean in_image = true;
                    for( int k = 0; k < 4; k++ )
                    {
                        if( ! isTransformedPointInImage( verticies[k] ) )
                        {
                            in_image = false;
                        }
                    }
                    
                    if( in_image )
                    {
                        this.image_data[i][j] = integrateOverPolygon( verticies, integration_step, use_bilinear );
                    }
                    else
                    {
                        this.image_data[i][j] = Color.BLACK.getRGB();
                    }
                }
            }
            updateImage();
            return;
        }

        // Provede algoritmus
        for( int i = 0; i < this.image_width; i++ )
        {
            for( int j = 0; j < this.image_height; j++ )
            {
                FloatPoint X_prime = new FloatPoint();
                X_prime = computeXPrime( new FloatPoint( i, j ), a, b, p );

                // Pokud je zrojový bod v obrázku vrátí odpovídající barvu pixelu, jinak vrátí černou.
                if( isTransformedPointInImage(X_prime) )
                {
                    // Použití bilineární interpolace, pokud je nastaveno její použití.
                    if( use_bilinear )
                    {
                        this.image_data[i][j] = bilinear_interpolation( X_prime.x, X_prime.y );
                    }
                    else
                    {
                        this.image_data[i][j] = this.other_panel.image_data[ Math.round( X_prime.x ) ][ Math.round( X_prime.y ) ];
                    }
                }
                else
                {
                    this.image_data[i][j] = Color.BLACK.getRGB();
                }
            }
        }
        updateImage();
    }

    private boolean isTransformedPointInImage( FloatPoint p )
    {
        return p.x <= this.image_width - 1 && p.x >= 0 && p.y <= this.image_height - 1 && p.y >= 0;
    }

    // Pomocná funkce, pro výpočet proměné u
    private float computeU( FloatPoint X, Point P, Point Q )
    {
        float num = ( X.x - P.x ) * ( Q.x - P.x ) + ( X.y - P.y ) * ( Q.y - P.y );
        float dom =  (float) ( Math.pow( Q.x - P.x, 2 ) + Math.pow( Q.y - P.y, 2 ) );
        return num / dom;
    }

    // Pomocná funkce, pro výpočet proměné v
    private float computeV( FloatPoint X, Point P, Point Q )
    {
        float num = ( X.x - P.x ) * ( P.y - Q.y ) + ( X.y - P.y ) * ( Q.x - P.x );
        float dom = (float) Math.sqrt( Math.pow( Q.x - P.x, 2 ) + Math.pow( Q.y - P.y, 2 ) );
        return num / dom;
    }

    // Pomocná funkce, pro výpočet proměné X_i_prime
    private FloatPoint computeXiPrime( float u, float v, Point P_prime, Point Q_prime )
    {
        FloatPoint X_prime = new FloatPoint();
        float norm = (float) Math.sqrt( Math.pow( Q_prime.x - P_prime.x, 2) + Math.pow( Q_prime.y - P_prime.y, 2) );
        X_prime.x = P_prime.x + u * ( Q_prime.x - P_prime.x ) + v * ( P_prime.y - Q_prime.y ) / norm;
        X_prime.y = P_prime.y + u * ( Q_prime.y - P_prime.y ) + v * ( Q_prime.x - P_prime.x ) / norm;
        return X_prime;
    }

    // Pomocná funkce, pro výpočet vzdálenosti bodu od úsečky.
    private float distanceToLine( FloatPoint X, Line line, float u, float v )
    {
        if (u < 0)
        {
            return X.distanceToPoint(line.p1);
        }
        if (u > 1)
        {
            return X.distanceToPoint(line.p2);
        }
        return Math.abs(v);
    }

    // Vrátí X_prime pro dané i a j.
    private FloatPoint computeXPrime( FloatPoint X, float a, float b, float p)
    {
        // Pokud nejsou uložené žádné úsečky, výsledný bod má být stejný jako zdrojový.
        if( lines.isEmpty() )
        {
            return X;
        }

        FloatPoint displacement_sum = new FloatPoint(0, 0);
        float weight_sum = 0.0f;
        for( int k = 0; k < lines.size(); k++  )
        {
            Line line = lines.get(k);
            float u = computeU( X, line.p1, line.p2 );
            float v = computeV( X, line.p1, line.p2 );
            FloatPoint X_i_prime = computeXiPrime(u, v, this.other_panel.lines.get(k).p1, this.other_panel.lines.get(k).p2 );
            FloatPoint displacement = X_i_prime.subtract( X );
            float dist = distanceToLine(X, line, u, v);
            float weight = (float) Math.pow( Math.pow( line.length(), p) / ( a + dist ) , b);
            displacement_sum.add( displacement.times( weight ) );
            weight_sum += weight;
        }
        return X.plus( displacement_sum.over( weight_sum ) );
    }

    // Implementace bilineární interpolace.
    private int bilinear_interpolation( float x , float y )
    {
        int x0 = Math.round( (float) Math.floor( x ) );
        int x1 = Math.round( (float) Math.ceil( x ) );
        int y0 = Math.round( (float) Math.floor( y ) );
        int y1 = Math.round( (float) Math.ceil( y ) );

        int[] f00 = new int[3];
        int[] f10 = new int[3];
        int[] f01 = new int[3];
        int[] f11 = new int[3];

        int fx0[] = new int[3];
        int fx1[] = new int[3];
        int fxy[] = new int[3];

        f00[0] = getRed( this.other_panel.image_data[x0][y0] );
        f10[0] = getRed( this.other_panel.image_data[x1][y0] );
        f01[0] = getRed( this.other_panel.image_data[x0][y1] );
        f11[0] = getRed( this.other_panel.image_data[x1][y1] );

        f00[1] = getGreen( this.other_panel.image_data[x0][y0] );
        f10[1] = getGreen( this.other_panel.image_data[x1][y0] );
        f01[1] = getGreen( this.other_panel.image_data[x0][y1] );
        f11[1] = getGreen( this.other_panel.image_data[x1][y1] );

        f00[2] = getBlue( this.other_panel.image_data[x0][y0] );
        f10[2] = getBlue( this.other_panel.image_data[x1][y0] );
        f01[2] = getBlue( this.other_panel.image_data[x0][y1] );
        f11[2] = getBlue( this.other_panel.image_data[x1][y1] );

        for( int i = 0; i < 3; i++)
        {          
            fx0[i] = f00[i] + (int) ( ( x - x0 ) * ( f10[i] - f00[i] ) );
            fx1[i] = f01[i] + (int) ( ( x - x0 ) * ( f11[i] - f01[i] ) );
            fxy[i] = fx0[i] + (int) ( ( y - y0 ) * ( fx1[i] - fx0[i] ) );
        }

        return getRGB( fxy[0], fxy[1], fxy[2] );
    }

    private int getRed( int RGB )
    {
        return (RGB >> 16) & 0xFF;
    }

    private int getGreen( int RGB )
    {
        return (RGB >> 8) & 0xFF;
    }

    private int getBlue( int RGB )
    {
        return (RGB >> 0) & 0xFF;
    }

    private int getRGB( int Red, int Green, int Blue )
    {
        return new Color( Red, Green, Blue ).getRGB();
    }

    private int integrateOverPolygon( FloatPoint[] verticies, float step, boolean bilinear)
    {
        int color[] = new int[3];
        for( int i = 0; i < 3; i++ )
        {
            color[i] = 0;
        }

        float lower_bound = verticies[0].y;
        float upper_bound = verticies[0].y;
        float left_bound = verticies[0].x;
        float right_bound = verticies[0].x;
        
        for( int i = 1; i < 4; i++ )
        {
            lower_bound = Math.min( verticies[i].y, lower_bound );
            upper_bound = Math.max( verticies[i].y, upper_bound );
            left_bound = Math.min( verticies[i].x, left_bound );
            right_bound = Math.max( verticies[i].x, right_bound );
        }

        FloatPoint[] edge_normal_vectors = new FloatPoint[4];
        for( int i = 0; i < 4; i++ )
        {
            edge_normal_vectors[i] = new FloatPoint(0, 0);
            edge_normal_vectors[i].x = verticies[ ( i + 1 ) % 4 ].y - verticies[i].y;
            edge_normal_vectors[i].y = verticies[i].x - verticies[( i + 1 ) % 4 ].x;
        }

        float mesh_lower_bound = lower_bound - lower_bound % step;
        float mesh_upper_bound = upper_bound - upper_bound % step + step;
        float mesh_left_bound = left_bound - left_bound % step;
        float mesh_right_bound = right_bound - right_bound % step + step;
        
        int area_counter = 0;
        FloatPoint current_point = new FloatPoint(0, 0);
        boolean is_inside = false;
        for( int j = 0; mesh_lower_bound + j * step < mesh_upper_bound; j++ )
        {
            for( int i = 0; mesh_left_bound + i * step < mesh_right_bound; i++ )
            {
                is_inside = true;
                current_point.x = mesh_left_bound + i * step + step/2;
                current_point.y = mesh_lower_bound + j * step + step/2;

                for( int k = 0; k < 4; k++ )
                {
                    if( edgeFunction(   new FloatPoint( current_point.x, current_point.y ),
                                        edge_normal_vectors[k],
                                        verticies[k] )
                        < 0 )
                    {
                        is_inside = false;
                    }
                }

                if( is_inside )
                {
                    area_counter++;
                    if( bilinear )
                    {
                        color[0] += getRed( bilinear_interpolation( current_point.x, current_point.y ) );
                        color[1] += getGreen( bilinear_interpolation( current_point.x, current_point.y ) );
                        color[2] += getBlue( bilinear_interpolation( current_point.x, current_point.y ) );
                    }
                    else
                    {
                        color[0] += getRed( this.other_panel.image_data[ Math.round( current_point.x ) ][ Math.round( current_point.y ) ] );
                        color[1] += getGreen( this.other_panel.image_data[ Math.round( current_point.x ) ][ Math.round( current_point.y ) ] );
                        color[2] += getBlue( this.other_panel.image_data[ Math.round( current_point.x ) ][ Math.round( current_point.y ) ] );
                    }
                }
            }
        }

        if( area_counter != 0 )
        {
            color[0] /= area_counter;
            color[1] /= area_counter;
            color[2] /= area_counter;
        }
        else
        {
            return Color.CYAN.getRGB();
        }

        return getRGB( color[0], color[1], color[2]);
    }

    float edgeFunction( FloatPoint point, FloatPoint normal, FloatPoint line_point)
    {
        return ( point.x - line_point.x ) * normal.x + (point.y - line_point.y ) * normal.y;
    }

    // Pomocná třída bodu se souřadnicemi typu float
    class FloatPoint
    {
        public FloatPoint()
        {
            this.x = 0.0f;
            this.y = 0.0f;
        }

        public FloatPoint( float x, float y )
        {
            this.x = x;
            this.y = y;
        }

        public FloatPoint subtract( FloatPoint p )
        {
            return new FloatPoint( this.x - p.x, this.y - p.y );
        }

        public FloatPoint subtract( Point p )
        {
            return new FloatPoint( this.x - p.x, this.y - p.y );
        }

        public float distanceToPoint( Point p )
        {
            return (float) Math.sqrt( Math.pow( this.x - p.x, 2) + Math.pow( this.y - p.y, 2) );
        }

        public void add( FloatPoint p )
        {
            this.x += p.x;
            this.y += p.y;
        }

        public FloatPoint plus( FloatPoint p )
        {
            return new FloatPoint( this.x + p.x, this.y + p.y );
        }

        public FloatPoint over( float div )
        {
            return new FloatPoint( this.x / div, this.y / div );
        }

        public FloatPoint times( float mult )
        {
            return new FloatPoint( this.x * mult, this.y * mult );
        }

        public float x;
        public float y;
    }
}