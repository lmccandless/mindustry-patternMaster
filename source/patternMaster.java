import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class patternMaster extends PApplet {

/*
    Written for Processing 3
 patternMaster.pde 
 render mindustry png previews from the copy&paste format files 
 use the paste file as a launch argument or drag and drop it onto executable 
 for copy&paste indielm server mod
 by Desktop
 */
String [] atlas;
PImage spriteSheet;
float drawScale = 2;
final int tileSize = 8;

public void settings() {
  size(1, 1, P2D);
}

Pattern pattern;
String filename = "3xdur"; // a test file used if no arguments
public void setup() {
  surface.setVisible(false);
  if (args!=null) { 
    filename = args[0];
    filename = filename.replace(".txt", "");
  }

  spriteSheet = loadImage("sprites.png");
  atlas = loadStrings("sprites.atlas");  
  pattern = new Pattern(filename + ".txt");
}

// Returns sprite from the internal mindustry block name
// Uses sprites.atlas to retrieve  sprite location and size of blockname
public PImage getSprite(String name) {
  // Loop over all  lines containing block names
  // breaks with i set to  line number matching name
  int i;
  for (i = 6; i < atlas.length; i+= 7) {  
    String a = atlas[i].trim();
    if (a.equals(name)) break;
  }
  if (i > atlas.length) i = 14; // use "blank" sprite if name not found

  // xy line is 2 lines past the sprite name
  // size is 3 lines past. TODO: fully support larger sprites in Pattern
  String []xy = atlas[i+2].split(" ");
  xy[3] = xy[3].replaceAll(",", "");
  int x = Integer.parseInt(xy[3]);
  int y = Integer.parseInt(xy[4]);

  String []size = atlas[i+3].split(" ");
  size[3] = size[3].replaceAll(",", "");
  int w = Integer.parseInt(size[3]);
  int h = Integer.parseInt(size[4]); 

  return spriteSheet.get(x, y, w, h);
}

class Pattern {
  int w, h;
  String [][]name;
  int [][]rotation;
  PGraphics pg;
  String [] lines;
  
  Pattern(String patfile) {
    // load pattern file and return if failed
    lines = loadStrings(patfile);
    if (lines == null) return;
    
    parsePattern(); // parse lines into name[][] and rotation[][]
    render(); 
    pg.save(filename + ".png");
  }

  public void parsePattern() {
    // first two lines are the width and height of pattern
    w = Integer.parseInt(lines[0]);
    h = Integer.parseInt(lines[1]);
    name = new String[w][h];
    rotation = new int[w][h];
    
    for (int i = 0; i < w; i++) {
      String []tiles = lines[i+2].split(" ");
      int q = 0;
      for (String tile : tiles) {
        String [] properties = tile.split(",");
        name[i][q] = properties[0];
        rotation[i][q] = Integer.parseInt(properties[1]);
        q++;
      }
    }
  }

  public void render() {
    // initalize opengl surface of pattern's size * tileSize * drawSize without texture sampling
    pg = createGraphics((int)(w*tileSize*drawScale), (int)(h*tileSize*drawScale)  - tileSize*2, P2D);
    ((PGraphicsOpenGL)pg).textureSampling(2);
    
    pg.beginDraw();
    pg.translate(tileSize, -tileSize);
    pg.imageMode(CENTER);
    
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        pg.pushMatrix();
        pg.translate(x*tileSize*drawScale, y*tileSize*drawScale);
        pg.rotate((rotation[x][y] / 4.0f) * TWO_PI); // TODO: check if this matches the rotation used in game
        PImage sprite = getSprite(name[x][y]);
        pg.image(getSprite(name[x][y]), 0, 0, sprite.width*drawScale, sprite.height*drawScale);
        pg.popMatrix();
      }
    }
    pg.endDraw();
  }
}

public void draw(){
  surface.setVisible(false);
  exit();
}

/*void draw(){
 background(0);
 println(pattern.w, pattern.h);
 surface.setSize((int)(pattern.w*tileSize*drawScale) - tileSize/2, (int)(pattern.h*tileSize*drawScale)  - tileSize*2);
 // pattern.render(pg);
 image(pattern.pg,0,0);
 pattern.pg.save(filename + ".png");
 exit();
 }*/
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "patternMaster" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
