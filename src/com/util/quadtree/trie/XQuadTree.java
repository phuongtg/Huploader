package com.util.quadtree.trie;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.util.XConstants;

/**
 * This QuadTree is a trie-based tree, which means the Quad Tree split the space evenly.
 * So it only bases on the space scope and the minimum size of the subspace.
 * TODO Acutally, the depth limitation should be added besides the min size of subspace.  
 * @author dan
 *
 */
public class XQuadTree {
	
    // min width of subspace;
    private double min_size_of_subspace = 1;
    
    private Rectangle2D.Double m_rect;            // The area this QuadTree represents
    private String index = "";    
    private Point2D.Double offset = null;

    private XQuadTree m_tl_child = null;   // Top Left Child
    private XQuadTree m_tr_child = null;   // Top Right Child
    private XQuadTree m_bl_child = null;   // Bottom Left Child
    private XQuadTree m_br_child = null;   // Bottom Right Child	
	
	
	private int depth = 0;  
	    
    private boolean hasChild = true;
       
    /**
     * 
     * @param rect
     * @param min_size_of_subspace
     * @param offset: Indicate the shifted width and height 
     */
    public XQuadTree(Rectangle2D.Double rect,double min_size_of_subspace,Point2D.Double offsetPoint){
    	// shift the space to the first quadrant
    	if(offsetPoint != null){
    		this.offset = new Point2D.Double(offsetPoint.x,offsetPoint.y);  
    		System.out.println("offset: "+this.offset.toString());
    		if(rect.x < 0 || rect.y < 0){
    			this.m_rect = new Rectangle2D.Double((rect.x + offsetPoint.x),(rect.y + offsetPoint.y),rect.width,rect.height);	
    		}else{
    			this.m_rect = new Rectangle2D.Double((rect.x - offsetPoint.x),(rect.y - offsetPoint.y),rect.width,rect.height);
    		}
    		
    	}else{       	
        	this.m_rect = new Rectangle2D.Double(rect.x,rect.y,rect.width,rect.height);    	
    	}
    	//System.out.println("in xquadtree: "+m_rect.x+";"+m_rect.y+";"+m_rect.width+";"+m_rect.height+"========");
    	this.min_size_of_subspace = min_size_of_subspace;
    }
    /*
     * coding means how to name the subspace. 
     * 0: 00,01,10,11
     * 1: 0,1,2,3
     */
    public void buildTree(int coding){
    	this.splitSpace(this.m_rect, this.m_rect.getWidth(), 0,coding);
    }
    /**
     * Evenly split the space    
     * @param rect
     * @param size_of_subSpace
     * @param depth
     */
    public void splitSpace(Rectangle2D.Double rect,double size_of_subSpace, int depth,int coding){            	
    	
    	this.m_rect = rect;        
        this.depth = depth;
        // change > to >=, because it should split when the width is double of the subspace
        if (this.m_rect.getWidth() >= 2*this.min_size_of_subspace) {                    
        	
            double bi_width = m_rect.width / 2;
            double bi_height = m_rect.height / 2;
                        
            Point2D.Double mid = new Point2D.Double(m_rect.x + bi_width, m_rect.y + bi_height);
            
            m_tl_child = new XQuadTree(new Rectangle2D.Double(m_rect.x, m_rect.y, bi_width, bi_height),min_size_of_subspace,null);           
            m_tr_child = new XQuadTree(new Rectangle2D.Double(mid.x, m_rect.y,bi_width, bi_height),min_size_of_subspace,null);
            m_bl_child = new XQuadTree(new Rectangle2D.Double(m_rect.x, mid.y, bi_width, bi_height),min_size_of_subspace,null);
            m_br_child = new XQuadTree(new Rectangle2D.Double(mid.x, mid.y,bi_width, bi_height),min_size_of_subspace,null);
              
            
            if (coding == XConstants.ENCODING_BINARY){
                this.m_tl_child.index = this.index+"01";
                this.m_tr_child.index = this.index+"11";
                this.m_bl_child.index = this.index+"00";
                this.m_br_child.index = this.index+"10";            	
            }else if(coding == XConstants.ENCODING_DECIMAL){
                this.m_tl_child.index = this.index+"1";
                this.m_tr_child.index = this.index+"3";
                this.m_bl_child.index = this.index+"0";
                this.m_br_child.index = this.index+"2"; 
            }           	
                        
            this.m_tl_child.splitSpace(new Rectangle2D.Double(m_rect.x, m_rect.y, bi_width, bi_height),bi_width,this.depth+1,coding);            
            this.m_tr_child.splitSpace(new Rectangle2D.Double(mid.x, m_rect.y,bi_width, bi_height),bi_width,this.depth+1,coding);
            this.m_bl_child.splitSpace(new Rectangle2D.Double(m_rect.x, mid.y, bi_width, bi_height),bi_width,this.depth+1,coding);
            this.m_br_child.splitSpace(new Rectangle2D.Double(mid.x, mid.y,bi_width, bi_height),bi_width,this.depth+1,coding);                         
           
            this.hasChild = true;            
        }
        else {
            this.m_tl_child = null;
            this.m_tr_child = null;
            this.m_bl_child = null;
            this.m_br_child = null;
            this.hasChild = false;            
        }        
        
    }
    /**
     * This is because the space is normalized, so the point should be normalized as well.
     * @return
     */
    public double[] normalize(double x, double y){
    	double[] result = new double[]{x,y};
    	if(this.offset != null){   		
    		if(x<0 || y < 0){
    			result[0] += this.offset.x;
        		result[1] += this.offset.y;	
    		}else{
    			result[0] -= this.offset.x;
        		result[1] -= this.offset.y;	    			
    		}    		
    	}
    	
    	
    	return result; 
    }
    
    
    /**
     * It is called by locate. It is to determine whether the point is in the subspace or not.
     * @param x increase from left to right
     * @param y increase from top to bottom 
     * @return
     */
    public boolean isInside(double x, double y) {
    	double[] normalized = this.normalize(x, y);
    	x = normalized[0];
    	y = normalized[1];
    	double x1 = this.m_rect.getX();
    	double x2 = this.m_rect.getX()+this.m_rect.getWidth();
    	double y1 = this.m_rect.getY();
    	double y2 = this.m_rect.getY()+this.m_rect.getHeight();
    	//System.out.println(x+","+y);
    	//System.out.println(x1+","+y1+");("+x2+","+y2);
        if ((x < x1) || (x > x2)) {        	
            return false;
        }
        if ((y < y1) || (y > y2)) {        	
        	return false;
        }        
        return true;
    }
    
    /**
     * Determine the subspace which the point belongs to.
     * It is used to get the point's index
     * @param x
     * @param y
     * @return
     */  
    public XQuadTree locate(double x, double y) {
    	//double[] normalized = this.normalize(x, y);
    	//System.out.println();
    	//x = normalized[0];
    	//y = normalized[1];   	
    	
        if (this.hasChild) {
            // check children
            if (this.m_tl_child.isInside(x,y)) {            	
                return this.m_tl_child.locate(x,y);
            }
            if (this.m_tr_child.isInside(x,y)) {            	
                return this.m_tr_child.locate(x, y);
            }
            if (this.m_bl_child.isInside(x,y)) {            	
            	return this.m_bl_child.locate(x,y);
            }
            if (this.m_br_child.isInside(x,y)) {            	
                return this.m_br_child.locate(x,y);
            }
            return null;
        }
        else {
            return this;
        }
    }  
    /**
     * This is to find the subspace for the area which is defined by a point and a distance
     * Query: It is used to process the query which is to get the points within a certain distance of the given point 
     * @param x: x coordinate of the centre point
     * @param y: y coordinate of the centre point
     * @param radius: 
     * @return
     */
    public List<String> match(double x,double y, double radius){
    	double[] normalized = this.normalize(x, y);    	    	
    	Rectangle2D.Double rect = new Rectangle2D.Double(normalized[0]-radius,normalized[1]-radius,2*radius,2*radius);    	
        // If this quad doesn't intersect the items rectangle, do nothing
        if (!m_rect.intersects(rect)
        		&& !m_rect.contains(new Point2D.Double(rect.getX(),rect.getY()))){        	
        	return null;
        } 
        ArrayList<String> result = new ArrayList<String>();
        Queue<XQuadTree> qe = new LinkedList<XQuadTree>();
        qe.offer(this);
        while(qe != null){
        	
        	while(!qe.isEmpty() && qe.element().m_tl_child == null){
        		XQuadTree leaf = qe.poll();
        		result.add(leaf.index);        		
        	}
        	
        	if(qe.isEmpty())
        		break;
        	
        	XQuadTree leaf = qe.poll();        	
        	getDestinationTree(leaf,rect,qe);      	                	        	    
       }
        return result; 
    }    
  
    /**
     * This is to find the subspace for the area which is defined by a point and a distance
     * Query: It is used to process the query which is to get the points within a certain distance of the given point 
     * IMPORTANT: This is for hybrid index. so the normalization should be put before this function
     * @param item
     * @return the index(es) of subspaces
     */
    public List<XQuadTree> tileMatch(double x,double y, double radius){
    	// fix this, because the given point should be the center point not the upper-left point
    	//Rectangle2D.Double rect = new Rectangle2D.Double(normalized[0]-radius,normalized[1]-radius,2*radius,2*radius);
    	Rectangle2D.Double rect = new Rectangle2D.Double(x-radius,y-radius,2*radius,2*radius);
    	
        // If this quad doesn't intersect the items rectangle, do nothing
        if (!m_rect.intersects(rect)
        		&& !m_rect.contains(new Point2D.Double(rect.getX(),rect.getY()))){        	
        	return null;
        } 
        ArrayList<XQuadTree> result = new ArrayList<XQuadTree>();
        Queue<XQuadTree> qe = new LinkedList<XQuadTree>();
        qe.offer(this);
        while(qe != null){
        	
        	while(!qe.isEmpty() && qe.element().m_tl_child == null){
        		XQuadTree leaf = qe.poll();
        		result.add(leaf);        		
        	}
        	
        	if(qe.isEmpty())
        		break;
        	
        	XQuadTree leaf = qe.poll();        	
        	getDestinationTree(leaf,rect,qe);      	                	        	    
       }
        return result; 
    }        
    
    
    /**
     * It is called by match()
     * @param destTree
     * @param item
     * @param index: if the area locates one tree, index includes one index; 
     * 				 if the rectangle crosses two trees, index is the string combined wit 
     * @return
     */
    private void getDestinationTree(XQuadTree destTree,Rectangle2D.Double item,Queue<XQuadTree> queue)
    {
        // If a child can't contain an object, it will live in this Quad    	
        if (destTree.m_tl_child == null){        	
        	return;
        }
              	                
        boolean contain = false;
        if(destTree.m_tl_child.getM_rect().contains(item)){
        	destTree = destTree.m_tl_child;
        	contain = true;
        }else if(destTree.m_tr_child.getM_rect().contains(item)){
        	destTree = destTree.m_tr_child;
        	contain = true;
        }else if (destTree.m_bl_child.getM_rect().contains(item))
        {
            destTree = destTree.m_bl_child;
            contain = true;
        }else if (destTree.m_br_child.getM_rect().contains(item))
        {
            destTree = destTree.m_br_child;
            contain = true;
        }        
        
        if(contain){
        	queue.offer(destTree);        	
        }else{ // judge whether it is intersect   	
            if(destTree.m_tl_child.getM_rect().intersects(item)){            	            	
            	queue.offer(destTree.m_tl_child);
            }
            if(destTree.m_tr_child.getM_rect().intersects(item)){            	
            	queue.offer(destTree.m_tr_child);
            }
            if(destTree.m_bl_child.getM_rect().intersects(item)){                         
            	queue.offer(destTree.m_bl_child);
            }
            if(destTree.m_br_child.getM_rect().intersects(item)){            	
            	queue.offer(destTree.m_br_child);
            }            
        }
        
    }    
      
    public int size() {
        if (!this.hasChild) {
            return 1;
        }        
        return 1 + this.m_tl_child.size()+ this.m_tr_child.size() + this.m_bl_child.size() + this.m_br_child.size();
    }
    
    public void print(){
    	if(!this.hasChild){
    		 System.out.println("level:"+this.depth+" => " + this.m_rect.getX() + ", " + this.m_rect.getY()
    				 + ",("+ this.m_rect.getWidth()+","+this.m_rect.getHeight()+")" + this.index);
    	}else{
    		this.m_tl_child.print();
    		this.m_tr_child.print();
    		this.m_bl_child.print();
    		this.m_br_child.print();
    	}
    }
    
    
    public XQuadTree getLeaf(double x, double y) {
        if (!this.hasChild) {
            return this;
        }

        if (this.m_tl_child.isInside(x, y)) {
            return this.m_tl_child.getLeaf(x, y);
        }
        if (this.m_tr_child.isInside(x, y)) {
            return this.m_tr_child.getLeaf(x, y);
        }
        if (this.m_bl_child.isInside(x, y)) {
            return this.m_bl_child.getLeaf(x, y);
        }
        if (this.m_br_child.isInside(x, y)) {
            return this.m_br_child.getLeaf(x, y);
        }
        return null;
    }    


	public boolean isHasChild() {
		return hasChild;
	}


	public void setHasChild(boolean hasChild) {
		this.hasChild = hasChild;
	}

	public Rectangle2D.Double getM_rect() {
		return m_rect;
	}

	public void setM_rect(Rectangle2D.Double m_rect) {
		this.m_rect = m_rect;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public XQuadTree getM_tl_child() {
		return m_tl_child;
	}

	public void setM_tl_child(XQuadTree m_tl_child) {
		this.m_tl_child = m_tl_child;
	}

	public XQuadTree getM_tr_child() {
		return m_tr_child;
	}

	public void setM_tr_child(XQuadTree m_tr_child) {
		this.m_tr_child = m_tr_child;
	}

	public XQuadTree getM_bl_child() {
		return m_bl_child;
	}

	public void setM_bl_child(XQuadTree m_bl_child) {
		this.m_bl_child = m_bl_child;
	}

	public XQuadTree getM_br_child() {
		return m_br_child;
	}

	public void setM_br_child(XQuadTree m_br_child) {
		this.m_br_child = m_br_child;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}


 
	
//  public XQuadTree locate_debug(double x, double y) {
//  boolean isThere = false;
//  XQuadTree node = this;
//
//	if (this.hasChild) {
//      if (this.m_tl_child.isInside(x,y)) {
//      	if(x== 45.514565 && y == -73.690909) System.out.println("m_tl_child");
//          return this.m_tl_child.locate(x,y);
//      }
//      if (this.m_tr_child.isInside(x,y)) {
//      	if(x== 45.514565 && y == -73.690909) System.out.println("m_tr_child");
//          return this.m_tr_child.locate(x, y);
//      }
//      if (this.m_bl_child.isInside(x,y)) {
//      	if(x== 45.514565 && y == -73.690909) System.out.println("m_bl_child");
//      	return this.m_bl_child.locate(x,y);
//      }
//      if (this.m_br_child.isInside(x,y)) {
//      	if(x== 45.514565 && y == -73.690909) System.out.println("m_br_child");
//          return this.m_br_child.locate(x,y);
//      }
//      return null;
//  }
//  else {
//      return this;
//  }
//} 	
    
    
}
