  import java.awt.Graphics;
  import java.awt.Color;
  
  class Particle extends GUIObject {
        public Particle(int x,int y, Color c) {
            super(x,y,6,6);
            this.dx = 5.0 - Math.random()*10.0;
            this.dy = 5.0 - Math.random()*10.0;
            this.c = c;
        }
        public void paint(Graphics g) {
            g.setColor(c);
            g.fillOval(x-3,y-3,6,6);
        }
        public void tick() {
            translate((int)Math.round(dx),(int)Math.round(dy));
            this.dy += 0.1;
        }
        private double dx;
        private double dy;
        private Color c;
    }

