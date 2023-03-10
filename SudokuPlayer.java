/*
Students: Cheko Mkocheko and Alivia Kliesen
*/

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;

    /// --- AC-3 Constraint Satisfication --- ///
   
    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();
        
    /*
    Performs an AC3 search with forward checking and returns true if a solution is found
    */
    private final Boolean AC3Forward(){
        recursions = 0;

        // initializes globalDomains 
        int k=0; // represents an index in globalDomains
        while(k<81){ // iterates through each cell of Sudoku puzzle
            for(int i=0; i<9; i++){ // iterates through rows of Soduko board
                for(int j=0; j<9; j++){ // iterates through columns of Soduko board
                    if(vals[i][j] == 0){ // if the cell in vals is empty  
                        ArrayList<Integer> startDomain = new ArrayList<Integer>();
                        for(int m=0; m<9; m++){
                            startDomain.add(m+1);
                        }
                        globalDomains[k] = startDomain;
                    }
                    else{ // if the cells in vals already contains a number
                        ArrayList<Integer> domain = new ArrayList<Integer>();
                        domain.add(vals[i][j]);
                        globalDomains[k] = domain;
                    }
                    k++;
                }
            }
        }

        return forwardTrack(0,globalDomains); 
    }

    /*
    Recursively performs forward tracking on a sudoku puzzle 
    */
    private final Boolean forwardTrack(int cell, ArrayList<Integer>[] Domains){
        recursions +=1;

        ArrayList<Integer>[] cloneDomains = Arrays.copyOf(Domains, Domains.length);
    	
        if(cell > 80){ // reached end of Sudoku puzzle
            return true;
        }

        int rowNum = cell / 9;
        int colNum = cell % 9;
        
        ArrayList<Integer> deletedNeighbors = new ArrayList<Integer>(); // keep track of deleted neighbors incase 
                                                                        // Forward checking fails

        if(vals[rowNum][colNum] != 0){ // cell has already been assigned 
            return forwardTrack(cell+1, cloneDomains); // proceed to next cell
        }

        if(!AC3(cloneDomains)){ // AC failed, no solution found
            return false;
        }

        else{
            for(int tryV : cloneDomains[cell]){  
                Boolean forwardSuccess = true; 
                ArrayList<Integer>[] forwardDomains = Arrays.copyOf(cloneDomains, cloneDomains.length);
                for(int n : neighbors[cell]){ // loop through every neighbor of cell
                    int rowN = n / 9;
                    int colN = n % 9; 
                        if (vals[rowN][colN] == 0){ // neighbor is unassigned 
                            forwardDomains[n].remove(Integer.valueOf(tryV)); 
                            deletedNeighbors.add(n); // add neighbors whose domains shrank to list
                            if(forwardDomains[n].size() == 0){ // neighbor has empty domain because of assignment
                                vals[rowNum][colNum] = 0; // 
                                forwardSuccess = false;
                                break; 
                            }
                        }
                }
                if(forwardSuccess){ // cell assignment was valid for now
                    ArrayList<Integer> newDomain = new ArrayList<Integer>();
                    newDomain.add(tryV);
                    forwardDomains[cell] = newDomain; // replace cell's Domain with single assigned value
                    vals[rowNum][colNum] = tryV; // update board
                    // System.out.println("attempt to forward track");
                    if(forwardTrack(cell+1, forwardDomains)){
                        return true;
                    }
                    else{ // restore the domain; assignment was invalid
                        for(int i=0; i<deletedNeighbors.size(); i++){
                            forwardDomains[deletedNeighbors.get(i)].add(tryV); // restore neighbors domains
                            forwardDomains[cell] = cloneDomains[cell];// restore the cell's domain
                        }
                    }
                }
                vals[rowNum][colNum] = 0; // unassign the given cell
            }
        }
        return false;
    }

    /*
    Initializes data structures to run AC3
    */
    private final void AC3Init(){
        //Do NOT remove these lines (required for the GUI)
        board.Clear();
		recursions = 0;
        allDiff();

        // initializes globalDomains 
        int k=0; // represents an index in globalDomains
        while(k<81){
            for(int i=0; i<9; i++){ // iterates through rows of Soduko board
                for(int j=0; j<9; j++){ // iterates through columns of Soduko board
                    if(vals[i][j] == 0){ // if the cell in vals is empty  
                        ArrayList<Integer> startDomain = new ArrayList<Integer>();
                        for(int m=0; m<9; m++){
                            startDomain.add(m+1);
                        }
                        globalDomains[k] = startDomain;
                    }
                    else{ // if the cells in vals already contains a number
                        ArrayList<Integer> domain = new ArrayList<Integer>();
                        domain.add(vals[i][j]);
                        globalDomains[k] = domain;
                    }
                    k++;
                }
            }
        }

         // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0,globalDomains); 
        Finished(success);
    }

    

    /*
     *  This method defines constraints between a set of variables.
     *  Refer to the book for more details. You may change this method header.
     */


    // The method below:
    // Called by AC3Init()
    // Fills neighbors, globalQueue(using binary constraints)
    // private final void allDiff(int[] all){
    private final void allDiff(){
        int k = 0; // keep count of cells

        // row neigbhbors
        for(int i = 0; i<9; i++){
            for(int m = 0; m < 9; m++){
                ArrayList<Integer> rowneighbors = new ArrayList<Integer>(); // creates a list of neighbors for every row
                for(int n = i*9; n < i*9+9; n++){
                    int cell = n; 
                    if( cell!= i*9 + m){ // compute the cell
                        rowneighbors.add(cell);
                    }
                }
                neighbors[i*9 + m] = rowneighbors; 
            }
        }

            // column neighbors
            k = 0; // keep track of cells;
            while(k < 81){
                for(int i = 0; i < 9; i++){
                    ArrayList<Integer> colneighbors = new ArrayList<Integer>();
                    for(int j = 0; j < 9; j++){
                        int cell = j * 9 + i; // compute the value of a cell (k) in the entire column
                        if(cell != k){  // don't add the num itself
                            colneighbors.add(cell);
                        }
                    }
                    ArrayList<Integer> temp = neighbors[k];
                    temp.addAll(colneighbors);
                    neighbors[k]= temp;
                    k++; 
                }
            }

        //box neighbors
    
    //box row:
    // group boxes into regions 0 - 8,
    // fill in every box with it's members 0 - 80
    //every row should contain its box members from boxes 0 - 8
    //ArrayList<Integer>[] regions = new ArrayList[9];
    int[][] regions = new int[9][9];
        int x=0; // variable of Sudoku cell 
        int first = 0;
        for(int i=0; i<9; i++){ // iterates through each region in soduko board 
            int[] regionNeighbors = new int[9];
            int m = 0; // index for regionNeighbors int[]
            int count = 1; // keeps track of number of cells encountered in region from the same row
            while(x<(first+21)){ // keeps the bounds to the bottom right corner of every region
                if(count<4){ 
                    regionNeighbors[m] = x;
                    x++;
                    m++;
                    count++;
                }
                else{ // finished storing a row of 3 cells in region
                    x+=6; // increment cellnum by 6 to get to next row
                    count=1; // restart count
                }
            } // finished storing all cells for a region
            if(((i+1)%3) !=0) { // proceed to region to right
                first+=3;
                x-=18;
            }
            else{ // reached end of row of 3 regions, proceed to first region in row of regions below
                first+=21;
            }
            regions[i] = regionNeighbors;
        }
    

        // create neighbors from regions
        for(int i = 0; i < 9; i++){  // iterates through each region
            for(int m = 0; m < 9; m++){ // iterates through each number in a region
                int cell = regions[i][m]; // picks one cell from the region
                ArrayList<Integer> cellNeighbors = neighbors[cell];
                for(int n = 0; n < 9; n++){ // iterates through all the cell's neighbors in its region
                    int candidate = regions[i][n]; // grab a candidate from the row with all the members of a region
                    if(cellNeighbors.contains(candidate) || candidate == cell){ // add the number only if it is not the cell
                        // itself or is not in the neigbors
                        continue;
                    }
                    else{
                        cellNeighbors.add(candidate);
                    }
                }
                neighbors[cell] = cellNeighbors; 
            }

        }
        // end of create neighbors from regions
        // create arcs: start from here
        // pick a cell, pull all its neighbors, create an arc with every neighbor
        // add the arc to the queue
        for(int i = 0; i < 81; i++){
            ArrayList<Integer> cellNeighbors = neighbors[i];
            for(int n = 0; n < cellNeighbors.size(); n++){
                int neighborCell = cellNeighbors.get(n);
                Arc arc = new Arc(i, neighborCell);
                globalQueue.add(arc);
            }
        }
    }


    /*
     * This is the backtracking algorithm. If you change this method header, you will have
     * to update the calls to this method. 
     */
    private final boolean backtrack(int cell, ArrayList<Integer>[] Domains) {

    	//Do NOT remove
    	recursions +=1;

        ArrayList<Integer>[] cloneDomains = Arrays.copyOf(Domains, Domains.length);
    	
        if(cell > 80){ // reached end of Sudoku
            return true;
        }

        int rowNum = cell / 9;
        int colNum = cell % 9;

        if(vals[rowNum][colNum] != 0){ // already assigned cell
            return backtrack(cell+1, cloneDomains); // proceed to next cell
        }

        if(!AC3(cloneDomains)){
            return false;
        }

        else{
            for(int v : cloneDomains[cell]){
                vals[rowNum][colNum] = v;
     
                ArrayList<Integer> newDomain = new ArrayList<Integer>();
                newDomain.add(v);
                cloneDomains[cell] = newDomain;
                
                if(backtrack(cell+1, cloneDomains)){ 
                    return true;
                }
                else{ // change v to something else
                    vals[rowNum][colNum] = 0;   
                }
            }
        }
        return false;
    }

    
    /*
     * This is the actual AC3 Algorithm. You may change this method header.
     */
    private final boolean AC3(ArrayList<Integer>[] Domains) {
        Queue<Arc> Q = new LinkedList<Arc>(globalQueue);

        
    	while(Q.size()!=0){
            Arc currArc = Q.poll(); // originally remove()
            boolean reviseResult = Revise(currArc, Domains); 
            if(Domains[currArc.Xi].size() == 0){ // no value to assign (xi's domain is empty)
                return false;
            }
            if(reviseResult){ // an update occured so t.Xi's domain shrank
                ArrayList<Integer> xNeighbors = neighbors[currArc.Xi]; // get xi's neighbors 
                for(int i=0; i<xNeighbors.size(); i++){ // create new arcs with an update of x domain
                    Arc neighborArc = new Arc(xNeighbors.get(i), currArc.Xi); 
                    Q.add(neighborArc); 
                }
            }
        }
		return true;
    }
    
    

    /*
     * This is the Revise() procedure. You may change this method header.
     * recommendation: print debugging for this method 
     * alternate: iterate through xi's domain, if len(xj's domain) = 1 and the single value = xi's currval, then remove xi's currval
     */
     private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){

        boolean revised = false;
        ArrayList<Integer>[] domainsReviseCopy = Arrays.copyOf(Domains, Domains.length);
        ArrayList<Integer> d = new ArrayList<Integer>(Domains[t.Xi]); // temp array 
    	for(int i=0; i< domainsReviseCopy[t.Xi].size() ; i++){ // every value in tXi doimain
             int currVal = domainsReviseCopy[t.Xi].get(i);    
             if(domainsReviseCopy[t.Xj].size() == 1){
                 int singleVal = domainsReviseCopy[t.Xj].get(0);
                 if(singleVal == currVal){ // invalid assignment
                    d.remove(Integer.valueOf(currVal)); 
                    Domains[t.Xi] = d; 
                    revised = true; // domain of Xi has been updated
                 }
             }
         }
         return revised;
 	}
  
     /*
      * This is where you will write your custom solver. 
      * You should not change this method header.
      */
    private final void customSolver(){
    	   
    	   //set 'success' to true if a successful board    
    	   //is found and false otherwise.
    	   boolean success = true; 
		   board.Clear();
           allDiff();
	        
	        System.out.println("Running custom algorithm");

            success = AC3Forward(); // runs AC3 algorithm with forward checking
	       
            Finished(success);
         }
        
    	       



    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){
        
        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
	        for(int j=0; j<9; j++)
	        	System.out.print(vals[i][j]+" ");
	        System.out.println();
        }*/
        
        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }

    

    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
        	AC3Init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);
        
        long start=0, end=0;
       
        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                	start = System.currentTimeMillis();
                	AC3Init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                	start = System.currentTimeMillis();
                	customSolver();
                	end = System.currentTimeMillis();
                    break;
            }
            
            CheckSolution();
            
            if(!gui)
            	System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
            	vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
            	vals[1] = new int[] {3,0,0,0,1,0,0,5,0};  
            	vals[2] = new int[] {0,0,6,0,0,0,1,0,0};  
            	vals[3] = new int[] {7,0,0,0,9,0,0,0,0};    
            	vals[4] = new int[] {0,4,0,6,0,3,0,0,0};    
            	vals[5] = new int[] {0,0,3,0,0,2,0,0,0};    
            	vals[6] = new int[] {5,0,0,0,8,0,7,0,0};    
            	vals[7] = new int[] {0,0,7,0,0,0,0,0,5};    
            	vals[8] = new int[] {0,0,0,0,0,0,0,9,8};  
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){
    	
    	if(success) {
            board.writeVals();
            //board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
            board.showMessage("Solved in " + myformat.format(recursions) + " recursive ops");

    	} else {
            //board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        	board.showMessage("No valid configuration found");
        }
         recursions = 0;
       
    }
 
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("Gui? y or n ");
        char g=scan.nextLine().charAt(0);

        if (g=='n')
            gui = false;
        else
            gui = true;
        
        if(gui) {
        	System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

	        char c = '*';

	        while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
	        	c = scan.nextLine().charAt(0);
	            if(c=='e')
	                level = difficulty.valueOf("easy");
	            else if(c=='m')
	                level = difficulty.valueOf("medium");
	            else if(c=='h')
	                level = difficulty.valueOf("hard");
	            else if(c=='r')
	                level = difficulty.valueOf("random");
	            else{
	                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
	            }
	        }
	        
	        SudokuPlayer app = new SudokuPlayer();
	        app.run();
	        
        }
        else { //no gui
        	
        	boolean again = true;
        
        	int numiters = 0;
        	long starttime, endtime, totaltime=0;
        
        	while(again) {
        
        		numiters++;
        		System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

        		char c = '*';

		        while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
		        	c = scan.nextLine().charAt(0);
		            if(c=='e')
		                level = difficulty.valueOf("easy");
		            else if(c=='m')
		                level = difficulty.valueOf("medium");
		            else if(c=='h')
		                level = difficulty.valueOf("hard");
		            else if(c=='r')
		                level = difficulty.valueOf("random");
		            else{
		                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
		            }
	            
		        }

	            System.out.println("Algorithm? AC3 (1) or Custom (2)");
	            if(scan.nextInt()==1)
	                alg = algorithm.valueOf("AC3");
	            else
	                alg = algorithm.valueOf("Custom");
	        
	
		        SudokuPlayer app = new SudokuPlayer();
		       
		        starttime = System.currentTimeMillis();
		        
		        app.run();
		        
		        endtime = System.currentTimeMillis();
		        
		        totaltime += (endtime-starttime);
	        
	       
	        	System.out.println("quit(0), run again(1)");
	        	if (scan.nextInt()==1)
	        		again=true;
	        	else
	        		again=false;
	        
	        	scan.nextLine();
	        
        	}
        
        	System.out.println("average time over "+numiters+" iterations: "+(totaltime/numiters));
        }
    
        
        
        scan.close();
    }



    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

           // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);






            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");
    
    //For printing
	static int recursions;
}




