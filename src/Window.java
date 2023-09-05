import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Window extends JFrame
{
    // Jméno systému
    String system_name;
    
    // Panely s obrázky
    ImagePanel source_image;
    ImagePanel edited_image;

    // Parametry warpingu
    float a = 1;
    float b = 1;
    float p = 1;
    float integration_step = 0.1f;


    public Window()
    {
        try
        {
            initialization();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Inicializace okna a všech potřebných proměnných.
    private void initialization() throws Exception
    {
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        setSize( new Dimension( screen_size.width/2, screen_size.height/2 ) );
        setLocation( screen_size.width/4, screen_size.height/4 );
        setTitle("Warping");
        setVisible(true);
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        system_name = System.getProperty( "os.name" );

        // Inicializace obrázkových panelů
        setLayout( new GridBagLayout() );
        source_image = new ImagePanel();
        edited_image = new ImagePanel();
        source_image.addSecondPanel( edited_image );
        edited_image.addSecondPanel( source_image );
        source_image.addMouseMotionListener( new Mouse() );
        source_image.addMouseListener( new Mouse() );
        edited_image.addMouseMotionListener( new Mouse() );
        edited_image.addMouseListener( new Mouse() );

        add( source_image, new GridBagConstraints(  0,
                                                    1,
                                                    1,
                                                    1,
                                                    0.5,
                                                    0.8,
                                                    GridBagConstraints.CENTER,
                                                    GridBagConstraints.BOTH,
                                                    new Insets(5, 5, 5, 5),
                                                    0,
                                                    0) );

        add( edited_image, new GridBagConstraints(  1,
                                                    1,
                                                    1,
                                                    1,
                                                    0.5,
                                                    0.8,
                                                    GridBagConstraints.CENTER,
                                                    GridBagConstraints.BOTH,
                                                    new Insets(5, 5, 5, 5),
                                                    0,
                                                    0) );

        initMenuBar();

        validate();
        repaint();
        source_image.repaint();
        edited_image.repaint();
    }

    // Inicializace nabídky
    private void initMenuBar()
    {
        // Proměnné nabídky
        JMenuBar window_bar = new JMenuBar();
        JMenu file = new JMenu( "Soubor" );
        JMenuItem safe = new JMenuItem( "Uložit obrázek" );
        JMenuItem load_image = new JMenuItem( "Načíst obrázek" );
        JMenu warping = new JMenu( "Warping");
        JMenuItem warp = new JMenuItem( "Warping" );
        JMenuItem set_par = new JMenuItem( "Nastavení parametrů");
        JCheckBoxMenuItem bilinear = new JCheckBoxMenuItem( "Bilineární interpolace", false );
        JCheckBoxMenuItem antialiasing = new JCheckBoxMenuItem("Antialiasing", false );
        JMenu rest = new JMenu( "Ostatní" );
        JCheckBoxMenuItem lines_visible = new JCheckBoxMenuItem( "Čáry", true );
        JMenuItem help = new JMenuItem( "Nápověda" );


        setJMenuBar(window_bar);
        window_bar.add( file );
        file.add( load_image );
        file.add( safe );
        window_bar.add( warping );
        warping.add( warp );
        warping.add( set_par );
        warping.add( bilinear );
        warping.add( antialiasing );
        window_bar.add( rest );
        rest.add( lines_visible );
        rest.add( help );

        // Nastavení akcí při stisknutí tlačítek nabídky
        load_image.addActionListener(   new ActionListener()
                                        {
                                            @Override
                                            public void actionPerformed( ActionEvent e )
                                            {
                                                loadImage();
                                            }
                                        }
                                    );
        safe.addActionListener( new ActionListener()
                                {
                                    @Override
                                    public void actionPerformed( ActionEvent e )
                                    {
                                        safeImage();
                                    }
                                } );
        lines_visible.addActionListener(    new ActionListener()
                                            {
                                                @Override
                                                public void actionPerformed( ActionEvent e )
                                                {
                                                    source_image.lines_visible = lines_visible.getState();
                                                    edited_image.lines_visible = lines_visible.getState();
                                                    source_image.repaint();
                                                    edited_image.repaint();
                                                }
                                            } );
        warp.addActionListener( new ActionListener()
                                {
                                    @Override
                                    public void actionPerformed( ActionEvent e )
                                    {
                                        edited_image.warp( a, b, p, integration_step, bilinear.getState(), antialiasing.getState() );
                                        //edited_image.repaint();
                                    }
                                } );
        bilinear.addActionListener( new ActionListener()
                                    {
                                        @Override
                                        public void actionPerformed( ActionEvent e )
                                        {
                                            //edited_image.repaint();
                                        }
                                    } );
        antialiasing.addActionListener( new ActionListener()
                                    {
                                        @Override
                                        public void actionPerformed( ActionEvent e )
                                        {
                                            //edited_image.repaint();
                                        }
                                    } );
        set_par.addActionListener(  new ActionListener()
                                    {
                                        @Override
                                        public void actionPerformed( ActionEvent e )
                                        {
                                            setWarpParameters();
                                        }
                                    } );
        help.addActionListener(   new ActionListener()
                                    {
                                        @Override
                                        public void actionPerformed( ActionEvent e )
                                        {
                                            showHelp();
                                        }
                                    } );

        // Nastavení modifikátoru podle systému
        int modifier = 0;
        if( system_name.toLowerCase().contains( "win" ) )
        {
            modifier = KeyEvent.CTRL_DOWN_MASK;
        }
        else if( system_name.toLowerCase().contains( "linux" ) )
        {
            modifier = KeyEvent.ALT_DOWN_MASK;
        }
        else if( system_name.toLowerCase().contains( "mac" ) )
        {
            modifier = KeyEvent.META_DOWN_MASK;
        }

        // Nastavení klávesových zkratek
        safe.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_S, modifier ) );
        load_image.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_O, modifier ) );
        lines_visible.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_L, modifier ) );
        warp.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, modifier ) );
        bilinear.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_B, modifier ) );
        antialiasing.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A, modifier ) );
        set_par.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, modifier ) );
        help.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_H, modifier ) );
    }

    // Ukáže okno pro výběr obrázku.
    private void loadImage()
    {
        try
        {
            JFileChooser file_chooser = new JFileChooser();
            file_chooser.setCurrentDirectory(new File( "." ));
            file_chooser.setDialogTitle( "Načíst obrázek");
            file_chooser.setFileFilter( new Filter("jpeg jpg png") );

            if ( file_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                {
                    File source_file = file_chooser.getSelectedFile();
                    source_image.loadImage( ImageIO.read( source_file ) );
                    edited_image.loadImage( ImageIO.read( source_file ) );
                    repaint();
                }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Ukáže okno pro uložení upraveného obrázku.
    private void safeImage()
    {
        if( edited_image.image_data == null )
        {
            JOptionPane.showMessageDialog(null, "Není načten žádný obrázek.\nNelze nic uložit.", "Varování", JOptionPane.INFORMATION_MESSAGE );
            return;
        }
        try
        {
            JFileChooser file_chooser = new JFileChooser();
            file_chooser.setDialogType( JFileChooser.SAVE_DIALOG );
            file_chooser.setDialogTitle("Uložit");
            file_chooser.setSelectedFile( new File( "warping" ) );
            file_chooser.setCurrentDirectory(new File( "." ) );
            file_chooser.setFileFilter( new Filter( "jpg" ) );
            file_chooser.addChoosableFileFilter( new Filter( "png" ) );

            //file_chooser.setFileFilter( new FileNameExtensionFilter( "PNG Image (*.png)", ".png" ) );
            //file_chooser.addChoosableFileFilter( new FileNameExtensionFilter( "JPEG Image (*.jpg)", ".jpg" ) );
            if ( file_chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                File file = file_chooser.getSelectedFile();
                String file_extension = getFileExtension( file );
                FileFilter used_filter = file_chooser.getFileFilter();
                if( used_filter instanceof Filter )
                {
                    Filter custom_filter = (Filter) ( used_filter );
                    boolean has_extension = false;
                    for( String s:custom_filter.getFilteredExtension())
                    {
                        if( s.contains(file_extension) && file_extension != "" )
                        {
                            has_extension = true;
                        }
                    }
                    if( ! has_extension )
                    {
                        file_extension = custom_filter.getFilteredExtension()[0];
                        file = new File( file.toString() + "." + file_extension );
                    }
                } 
                ImageIO.write( edited_image.image, file_extension, file);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Pomocná funkce, která vrátí příponu souboru.
    private static String getFileExtension(File file)
    {
        String extension = "";
        String file_name = file.getName();
        int dot_index = file_name.lastIndexOf('.');
        if (dot_index > 0 && dot_index < file_name.length() - 1)
        {
            extension = file_name.substring(dot_index + 1).toLowerCase();
        }
        return extension;
    }

    // Ukáže dialogové okno, ve kterém uživatel zadadá parametry warpingu.
    private void setWarpParameters()
    {
        JPanel main_panel = new JPanel();
        main_panel.setLayout( new BoxLayout( main_panel, BoxLayout.Y_AXIS ) );

        JPanel horizontal_wrapper1 = new JPanel();
        JPanel horizontal_wrapper2 = new JPanel();
        JPanel horizontal_wrapper3 = new JPanel();
        JPanel horizontal_wrapper4 = new JPanel();

        horizontal_wrapper1.setLayout( new BoxLayout( horizontal_wrapper1, BoxLayout.X_AXIS ) );
        horizontal_wrapper2.setLayout( new BoxLayout( horizontal_wrapper2, BoxLayout.X_AXIS ) );
        horizontal_wrapper3.setLayout( new BoxLayout( horizontal_wrapper3, BoxLayout.X_AXIS ) );
        horizontal_wrapper4.setLayout( new BoxLayout( horizontal_wrapper4, BoxLayout.X_AXIS ) );

        JTextField text_field1 = new JTextField(null, Float.toString( a ),  10 );
        JTextField text_field2 = new JTextField(null, Float.toString( b ), 10 );
        JTextField text_field3 = new JTextField(null, Float.toString( p ), 10 );
        JTextField text_field4 = new JTextField(null, Float.toString( integration_step ), 10 );

        horizontal_wrapper1.add( new JLabel( "a: " ) );
        horizontal_wrapper2.add( new JLabel( "b: " ) );
        horizontal_wrapper3.add( new JLabel( "p: " ) );
        horizontal_wrapper4.add( new JLabel( "krok integrace: " ) );
        horizontal_wrapper1.add( text_field1 );
        horizontal_wrapper2.add( text_field2 );
        horizontal_wrapper3.add( text_field3 );
        horizontal_wrapper4.add( text_field4 );

        main_panel.add( horizontal_wrapper1 );
        main_panel.add( horizontal_wrapper2 );
        main_panel.add( horizontal_wrapper3 );
        main_panel.add( horizontal_wrapper4 );

        int result = JOptionPane.showConfirmDialog(null, main_panel, "Zadejte hodnoty", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION)
        {
            try {
                a = Float.parseFloat( text_field1.getText() );
                b = Float.parseFloat( text_field2.getText() );
                p = Float.parseFloat( text_field3.getText() );
                integration_step = Float.parseFloat( text_field4.getText() );

                if( a < 0 || b < 0 || p < 0 || integration_step < 0)
                    throw new IllegalArgumentException("Chyba: Zadávejte pouze nezáporná čísla!");
                if( integration_step > 1 )
                    throw new IllegalArgumentException( "Chyba: Krok integrace nemůže být větší než 1.");
            } catch( IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Varování", JOptionPane.WARNING_MESSAGE );
            }
        }
    }

    // Ukáže dialogové okno s nápovědou.
    private void showHelp()
    {
        JTextArea text = new JTextArea();
        String help_text =  "Akce myši:\n\n" +
                                "Vložení nové úsečky -\tNajeďte myší na pozici, kde chcete začít kreslit.\n\t\t" +
                                "Stiskněte levé tlačítko a táhněte myší. Nakonec\n\t\tuvolněte stisknuté tlačítko.\n" +
                                "Editace úsečky -\tMyší najeďte na bod, který chcete posunout.\n\t\t" +
                                "Stiskněte její levé tlačítko. Táhněte myší na\n\t\tnovou pozici a uvolněte tlačítko.\n" +
                                "Vymazání úsečky -\tNajeďte myší nad libovolný bod úsečky, kterou\n\t\tchcete smazat. " +
                                "Stiskněte pravé tlačítko myši.\n\n\n" + 
                                "Význam parametrů:\n\n" +
                                "a -\tUrčuje kontrolu bodů. Při hodnotách blízkých 0 body na\n\tusečkách zůstanou po " +
                                "warpingu na těchto úsečkách. " +
                                "Čím vyšší\n\thodnota, tím hladší je warping. Zároveň se ale zmenšuje naše\n\tkontrola " +
                                "toho, kam se body přesunou.\n" +
                                "b -\tUrčuje, jak rychle se zmenšuje vliv úseček se vzdáleností. Pro\n\tb = 0 jsou body " +
                                "ovlivňovány všemi úsačkami stejně. Pro velké\n\thodnoty jsou " +
                                "ovlivňovány pouze těmi nejbližšími. Nejužitečnější\n\thodnoty parametru jsou v intervalu [0.5; 2].\n" +
                                "p -\tUrčuje, jaký dopad má délka úseček. Pro p = 0 váha úsečky\n\tnezávisí na délce. "+
                                "Pro p = 1 pak mají větší váhu delší úsečky.\n\tDoporučené hodnoty jsou ty z [0; 1].";
        text.setFont( new Font("Arial", Font.PLAIN, 12) );
        text.setText(help_text);


        text.setBackground( UIManager.getColor("InternalFrame.background") );
        JOptionPane.showMessageDialog(null, text, "Nápověda", JOptionPane.PLAIN_MESSAGE );
    }

    // Vyřizuje akce myši.
    private class Mouse implements MouseListener, MouseMotionListener
    {
        // Rozhodne co dělat, při stisku některého tlačítka myši.
        public void mousePressed( MouseEvent e )
        {
            Object source = e.getSource();
            if( source instanceof ImagePanel )
            {
                ImagePanel panel = (ImagePanel) source;

                if( panel.image_data == null )
                {
                    return;
                }

                if( e.getButton() == MouseEvent.BUTTON1 )
                {
                    if( panel.lines_visible )
                    {
                        if( !panel.fetchPoint( e.getPoint() ) )
                        {
                            if( !panel.isMouseOutOfBounds( e.getPoint() ))
                            {
                                panel.startLine( e.getPoint(), true );
                            }
                        }
                    }
                }

                else if( e.getButton() == MouseEvent.BUTTON3)
                {
                    if( panel.lines_visible )
                    {
                        panel.delete_line( e.getPoint() );
                    }
                }
            }
        }

        // Uvolní právě kreslenou/editovanou úsečku.
        public void mouseReleased( MouseEvent e )
        {
            Object source = e.getSource();
            if( source instanceof ImagePanel )
            {
                ImagePanel panel = (ImagePanel) source;
                if( panel.lines_visible )
                {
                    panel.release_line( true );
                }
            }
        }

        // Zařizuje aktualizaci kreslené/editované úsečky, při táhnutí myši.
        public void mouseDragged( MouseEvent e)
        {
            Object source = e.getSource();
            if( source instanceof ImagePanel )
            {
                ImagePanel panel = (ImagePanel) source;
                if( panel.lines_visible )
                {
                    if( !panel.isMouseOutOfBounds( e.getPoint() ) )
                    {
                        panel.updateDrawnLine( e.getPoint(), true );
                    }
                }
            }
        }

        public void mouseMoved( MouseEvent e )
        {}

        public void mouseClicked( MouseEvent e )
        {}
        
        public void mouseExited( MouseEvent e )
        {}

        public void mouseEntered( MouseEvent e )
        {}


    }
}

// Filtr používaný při načítání a ukládání obrázků
class Filter extends javax.swing.filechooser.FileFilter
{
    private String extensions;
    public Filter( String extensions )
    {
        this.extensions = extensions;
    }
    
    @Override
    public boolean accept( File file )
    {
        String ext = "";
        if( file != null )
        {
            String name = file.getName();
            int i = name.lastIndexOf( '.' );
            if( i > 0 && i < name.length() - 1 )
            {
                ext = name.substring( i + 1 ).toLowerCase();
            }
        }

        return extensions.contains(ext);
    }

    public String[] getFilteredExtension()
    {
        return extensions.split( " " );
    }

    @Override
    public String getDescription()
    {
        String description = "";
        String exten_array[] = extensions.split(" " );
        for( String ext:exten_array)
        {
            description += ext.toUpperCase() + " Image (*." + ext + ")" + ", ";
        }
        description = description.substring(0, description.length() - 2);
        return description;
    }
}