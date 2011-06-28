  import java.awt.Graphics;
  import java.awt.Color;
  
  class Particle {
        public Particle(int x,int y, Color c) {
            this.x = x;
            this.y = y;
            this.dx = 30 - (int)Math.floor(Math.random()*60);
            this.dy = 30 - (int)Math.floor(Math.random()*60);
            this.c = c;
        }
        public void paint(Graphics g) {
            g.setColor(c);
            g.fillOval(x-3,y-3,6,6);
        }
        public void tick() {
            this.x += dx;
            this.y += dy;
            this.dy += 2;
        }
        public int x;
        public int y;
        public int dx;
        public int dy;
        public Color c;
    }

