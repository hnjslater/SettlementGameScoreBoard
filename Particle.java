  import java.awt.Graphics;
  import java.awt.Color;
  
  class Particle extends GUIObject {
        public Particle(int x,int y, Color c,long birthday) {
            super(x,y,6,6);
            this.dx = 0.5 - Math.random();
            this.dy = 0.5 - Math.random();
            this.c = c;
            this.birthday = birthday;
        }
        public void paint(Graphics g, long time) {
            g.setColor(c);
            g.fillOval(getX(time),getY(time),6,6);
        }
        private double dx;
        private double dy;
        private Color c;
        long birthday;
        public int getX(long time) {
            return (int)(x + dx * (time - birthday)); 
        }
        public int getY(long time) {
            double u = dy;
            double t = time - birthday;
            double a = 0.0005;
            return (int)(y + u*t + 0.5*(a*Math.pow(t,2))); 
        }
    }

