package board.packaging;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.File;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App {
    public static Map<Integer, ArrayList<int[]>> create_A_r(int m, int n, int r, Rectangle[] rectangles) {
        Map<Integer, ArrayList<int[]>> A_r = new HashMap<>();
        for (int k = 0; k < r; k++) {
            ArrayList<int[]> positions = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (i + rectangles[k].h <= m && j + rectangles[k].w <= n) {
                        positions.add(new int[] {i, j});
                    }
                }
            }
            A_r.put(k, positions);
        }
        return A_r;
    }

    /*
    public static Map<Position, Map<Integer, ArrayList<int[]>>> create_B_ijr(int m, int n, int r, Rectangle[] rectangles, Map<Integer, ArrayList<int[]>> A_r) {
        Map<Position, Map<Integer, ArrayList<int[]>>> B_ijr = new HashMap<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Map<Integer, ArrayList<int[]>> rectangleMap =  new HashMap<>();
                for (int k = 0; k < r; k++) {
                    ArrayList<int[]> coveringRectangles = new ArrayList<>();
                    for (int[] pos : A_r.get(k)) {
                        if (i <= pos[0] + rectangles[k].h &&  j <= pos[1] + rectangles[k].w) {
                            coveringRectangles.add(pos);
                        }
                    }
                    rectangleMap.put((Integer) k, coveringRectangles);
                }
                Position pos = new Position(i, j);
                B_ijr.put(pos, rectangleMap);
            }
        }
        return B_ijr;
    }
    */
    

    public static class Rectangle {
        int h;
        int w;
        int c;
        Rectangle(int h, int w, int c) {
            this.h = h;
            this.w = w;
            this.c = c;
        }
    }
    public static class Position {
        int i;
        int j;

        public Position(int i, int j) {
            this.i = i;
            this.j = j;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            if (i == position.i && j == position.j) return true;
            return false;
        }

        @Override
        public int hashCode() {
            return i + 31 * j;
        }
    }

    public static MPObjective addObjective(MPSolver solver,
    int m, int n, int r,
    int[][] board,
    Rectangle[] rectangles,
    MPVariable[][][] x_ijr,
    MPVariable[][] y_ij,
    Map<Integer, ArrayList<int[]>> A_r, Map<Position, Map<Integer, ArrayList<int[]>>> B_ijr) {
        MPObjective objective = solver.objective();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                objective.setCoefficient(y_ij[i][j], board[i][j]);
            }
        }
        for (int k = 0; k < r; k++) {
            for (int[] pos : A_r.get(k)) {
                objective.setCoefficient(x_ijr[pos[0]][pos[1]][k], -rectangles[k].c);
            }
        }
        objective.setMaximization();
        return objective;
    }
/*
     public static void addConstraints(
            MPSolver solver,
            int m, int n, int r,
            int[][] board,
            Rectangle[] rectangles,
            MPVariable[][][] x_ijr,
            MPVariable[][] y_ij,
            Map<Integer, ArrayList<int[]>> A_r,
            Map<Position, Map<Integer, ArrayList<int[]>>> B_ijr) {
                //printDebugInfo(A_r, B_ijr);
                double infinity = java.lang.Double.POSITIVE_INFINITY;
                for (int k = 0; k < r; k++) {
                    //Constraint 2
                    MPConstraint rectangleUsageConstraint = solver.makeConstraint(0, 1, "Rectangle_" + k + "_Usage");
                    for (int[] pos : A_r.get(k)) {
                        System.out.println("Adding constraint for rectangle " + k + " at position " + pos[0] + "," + pos[1]);
                        rectangleUsageConstraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1);
                    }
                }

                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < n; j++) {
                        if (board[i][j] > 0) {
                            MPConstraint constraint = solver.makeConstraint(-infinity, 0, "");
                            constraint.setCoefficient(y_ij[i][j], -1.0);
                            for (int k = 0; k < r; k++) {
                                Position position = new Position(i, j);
                                Map<Integer, ArrayList<int[]>> posMap = B_ijr.get(position);
                                ArrayList<int[]> coveringPositions = posMap.getOrDefault(k, new ArrayList<>());
                                for (int[] pos : coveringPositions) {
                                    constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1.0);
                                }
                            }

                        } else if (board[i][j] < 0) {
                            MPConstraint constraint = solver.makeConstraint(0, infinity, "Negative_cell_" + i + "_" + j);
                            constraint.setCoefficient(y_ij[i][j], r);
                            for (int k = 0; k < r; k++) {
                                Position position = new Position(i, j);
                                Map<Integer, ArrayList<int[]>> posMap = B_ijr.get(position);
                                ArrayList<int[]> coveringPositions = posMap.getOrDefault(k, new ArrayList<>());
                                for (int[] pos : coveringPositions) {
                                    constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1.0);
                                }
                            }
                        }
                    }
                }
            }
 */



public static Map<Position, Map<Integer, ArrayList<int[]>>> create_B_ijr(int m, int n, int r, Rectangle[] rectangles, Map<Integer, ArrayList<int[]>> A_r) {
    Map<Position, Map<Integer, ArrayList<int[]>>> B_ijr = new HashMap<>();

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            Position pos = new Position(i, j);
            B_ijr.put(pos, new HashMap<>());
        }
    }

    for (int k = 0; k < r; k++) {
        for (int[] startPos : A_r.get(k)) {
            int startI = startPos[0];
            int startJ = startPos[1];

            for (int i = startI; i < startI + rectangles[k].h; i++) {
                for (int j = startJ; j < startJ + rectangles[k].w; j++) {
                    Position coveredPos = new Position(i, j);

                    Map<Integer, ArrayList<int[]>> rectMap = B_ijr.get(coveredPos);
                    if (!rectMap.containsKey(k)) {
                        rectMap.put(k, new ArrayList<>());
                    }
                    rectMap.get(k).add(startPos);
                }
            }
        }
    }

    return B_ijr;
}

    public static void addConstraints(
            MPSolver solver,
            int m, int n, int r,
            int[][] board,
            Rectangle[] rectangles,
            MPVariable[][][] x_ijr,
            MPVariable[][] y_ij,
            Map<Integer, ArrayList<int[]>> A_r,
            Map<Position, Map<Integer, ArrayList<int[]>>> B_ijr) {

        double infinity = java.lang.Double.POSITIVE_INFINITY;

        for (int k = 0; k < r; k++) {
            MPConstraint rectangleUsageConstraint = solver.makeConstraint(0, 1, "Rectangle_" + k + "_Usage");
            for (int[] pos : A_r.get(k)) {
                rectangleUsageConstraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1);
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Position position = new Position(i, j);

                if (board[i][j] > 0) {
                    MPConstraint constraint = solver.makeConstraint(-infinity, 0, "Positive_cell_" + i + "_" + j);
                    constraint.setCoefficient(y_ij[i][j], 1.0);  

                    Map<Integer, ArrayList<int[]>> rectMap = B_ijr.get(position);
                    for (int k = 0; k < r; k++) {
                        ArrayList<int[]> coveringPositions = rectMap.getOrDefault(k, new ArrayList<>());
                        for (int[] pos : coveringPositions) {
                            constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], -1.0);  
                        }
                    }
                } else if (board[i][j] < 0) {
                    MPConstraint constraint = solver.makeConstraint(0, infinity, "Negative_cell_" + i + "_" + j);
                    constraint.setCoefficient(y_ij[i][j], r);

                    Map<Integer, ArrayList<int[]>> rectMap = B_ijr.get(position);
                    for (int k = 0; k < r; k++) {
                        ArrayList<int[]> coveringPositions = rectMap.getOrDefault(k, new ArrayList<>());
                        for (int[] pos : coveringPositions) {
                            constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], -1.0);  
                        }
                    }
                }
            }
        }
    }
            private static void printDebugInfo(
                Map<Integer, ArrayList<int[]>> A_r,
                Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr) {
                
                System.out.println("\n=== A_r Debug ===");
                for (Integer rectIdx : A_r.keySet()) {
                    System.out.println("Rectangle " + rectIdx + " can start at:");
                    for (int[] pos : A_r.get(rectIdx)) {
                        System.out.printf("[%d,%d] ", pos[0], pos[1]);
                    }
                    System.out.println("\n");
                }
        
                System.out.println("\n=== B_ijr Debug ===");
                for (Integer rectIdx : B_ijr.keySet()) {
                    System.out.println("Rectangle " + rectIdx + " covers:");
                    Map<int[], ArrayList<int[]>> coverage = B_ijr.get(rectIdx);
                    for (int[] coveredPos : coverage.keySet()) {
                        System.out.printf("Position [%d,%d] is covered by starters:\n", 
                            coveredPos[0], coveredPos[1]);
                        for (int[] starter : coverage.get(coveredPos)) {
                            System.out.printf("[%d,%d] ", starter[0], starter[1]);
                        }
                        System.out.println();
                    }
                    System.out.println();
                }
            }
    public static void main( String[] args ) {
        long totalStartTime = System.currentTimeMillis();
        Scanner sc = new Scanner(System.in);
        try {
            String path = "src/dataset/test2/rec_20.txt";
            File file = new File(path);
            sc = new Scanner(file);
            sc.useDelimiter("[,\\s]+");
        } catch (Exception e) {
            System.err.println("Cannot read file");
            e.printStackTrace();
            return;
        }

        System.out.println( "Hello World!" );
        int m = sc.nextInt();
        int n = sc.nextInt();

        Loader.loadNativeLibraries();

        int[][] board = new int[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int value = sc.nextInt();
                board[i][j] = value;
            }
        }

        int r = sc.nextInt();

        Rectangle[] rectangles = new Rectangle[r];

        for (int i = 0; i < r; i++) {
            int h = sc.nextInt();
            int w = sc.nextInt();
            int c = sc.nextInt();
            rectangles[i] = new Rectangle(h, w, c);
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }

        for (int i = 0; i < r; i++) {
            System.out.println(rectangles[i].h + " " + rectangles[i].w + " " + rectangles[i].c);
        }

        sc.close();

        MPSolver solver = MPSolver.createSolver("CBC");
        if (solver == null) {
            System.out.println("Cannot create solver");
            return;
        }

        MPVariable[][][] x_ijr = new MPVariable[m][n][r];
        MPVariable[][] y_ij = new MPVariable[m][n];

        Map<Integer, ArrayList<int[]>> A_r = create_A_r(m, n, r, rectangles);
        Map<Position, Map<Integer, ArrayList<int[]>>> B_ijr = create_B_ijr(m, n, r, rectangles, A_r);

        //printDebugInfo(A_r, B_ijr);

        //Constraint 5
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < r; k++) {
                    x_ijr[i][j][k] = solver.makeIntVar(0, 1, "");
                }
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                y_ij[i][j] = solver.makeIntVar(0, 1, "");
            }
        }

        //for (int i = 0; i < )

        

        MPObjective objective = addObjective(solver, m, n, r, board, rectangles, x_ijr, y_ij, A_r, B_ijr);
        addConstraints(solver, m, n, r, board, rectangles, x_ijr, y_ij, A_r, B_ijr);
        //printProblemDetails(solver, m, n, r, board, rectangles, x_ijr, y_ij, A_r, B_ijr);
        System.out.println("Number of variable: " + solver.numVariables());
        System.out.println("Number of constraints: " + solver.numConstraints());
        
        long solverStartTime = System.currentTimeMillis();
        final MPSolver.ResultStatus resultStatus = solver.solve();
        long solverEndTime = System.currentTimeMillis();

        long totalEndTime = System.currentTimeMillis();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Optimal solution!:");
            System.out.println("Objective value: " + objective.value());
            System.out.println("Solution value: ");
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    System.out.print(y_ij[i][j].solutionValue());
                }
                System.out.println();
            }
            System.out.println();
            
        }
        System.out.println("Solver time: " + (solverEndTime - solverStartTime) + " ms");
        System.out.println("Total execution time: " + (totalEndTime - totalStartTime) + " ms");
    }
}
