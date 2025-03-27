package board.packaging;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
/**
 * Hello world!
 *
 */
public class App {
    private static void printProblemDetails(
        MPSolver solver,
        int m, int n, int r,
        int[][] board,
        Rectangle[] rectangles,
        MPVariable[][][] x_ijr,
        MPVariable[][] y_ij,
        Map<Integer, ArrayList<int[]>> A_r,
        Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr) {
    
    System.out.println("\n=== VARIABLES ===");
    System.out.println("1. y_ij variables (1 if cell i,j is uncovered):");
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            System.out.printf("y_%d%d (board value: %d)\n", i, j, board[i][j]);
        }
    }

    System.out.println("\n2. x_ijr variables (1 if rectangle r starts at position i,j):");
    for (int k = 0; k < r; k++) {
        System.out.printf("\nRectangle %d (h=%d, w=%d, cost=%d) can start at:\n", 
            k, rectangles[k].h, rectangles[k].w, rectangles[k].c);
        for (int[] pos : A_r.get(k)) {
            System.out.printf("x_%d%d%d ", pos[0], pos[1], k);
        }
        System.out.println();
    }

    System.out.println("\n=== CONSTRAINTS ===");
    System.out.println("1. Rectangle placement constraints (≤ 1):");
    for (int k = 0; k < r; k++) {
        System.out.printf("\nRectangle %d: ", k);
        for (int[] pos : A_r.get(k)) {
            System.out.printf("x_%d%d%d + ", pos[0], pos[1], k);
        }
        System.out.println("≤ 1");
    }

    System.out.println("\n2. Cell coverage constraints:");
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (board[i][j] > 0) {
                System.out.printf("\nCell (%d,%d) positive value=%d: y_%d%d ≤ ", 
                    i, j, board[i][j], i, j);
                for (int k = 0; k < r; k++) {
                    Map<int[], ArrayList<int[]>> posMap = B_ijr.get(k);
                    ArrayList<int[]> coveringPositions = posMap.getOrDefault(new int[]{i, j}, new ArrayList<>());
                    for (int[] pos : coveringPositions) {
                        System.out.printf("x_%d%d%d + ", pos[0], pos[1], k);
                    }
                }
                System.out.println();
            } else if (board[i][j] < 0) {
                System.out.printf("\nCell (%d,%d) negative value=%d: %dy_%d%d ≥ ", 
                    i, j, board[i][j], r, i, j);
                for (int k = 0; k < r; k++) {
                    Map<int[], ArrayList<int[]>> posMap = B_ijr.get(k);
                    ArrayList<int[]> coveringPositions = posMap.getOrDefault(new int[]{i, j}, new ArrayList<>());
                    for (int[] pos : coveringPositions) {
                        System.out.printf("x_%d%d%d + ", pos[0], pos[1], k);
                    }
                }
                System.out.println();
            }
        }
    }

    System.out.println("\n=== OBJECTIVE FUNCTION ===");
    System.out.print("Maximize: ");
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (board[i][j] != 0) {
                System.out.printf("%d*y_%d%d + ", board[i][j], i, j);
            }
        }
    }
    System.out.println();
    for (int k = 0; k < r; k++) {
        for (int[] pos : A_r.get(k)) {
            System.out.printf("-%d*x_%d%d%d + ", rectangles[k].c, pos[0], pos[1], k);
        }
    }
    System.out.println();
}
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

    public static Map<Integer, Map<int[], ArrayList<int[]>>> create_B_ijr(int m, int n, int r, Rectangle[] rectangles, Map<Integer, ArrayList<int[]>> A_r) {
        Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr = new HashMap<>();
        for (int k = 0; k < r; k++) {
            Map<int[], ArrayList<int[]>> posMap = new HashMap<>();
            for (int[] topLeft : A_r.get(k)) {
                for (int i = topLeft[0]; i < topLeft[0] + rectangles[k].h; i++) {
                    for (int j = topLeft[1]; j < topLeft[1] + rectangles[k].w; j++) {
                        int[] coveredPos = new int[]{i, j};
                        ArrayList<int[]> coveringPositions = posMap.computeIfAbsent(
                            coveredPos, 
                            k_ -> new ArrayList<>()
                        );
                        coveringPositions.add(topLeft);
                    }
                }
            }
            B_ijr.put(k, posMap);
        }
        return B_ijr;
    }
    

    public static class Rectangle {
        int h;
        int w;
        public int c;
        Rectangle(int h, int w, int c) {
            this.h = h;
            this.w = w;
            this.c = c;
        }
    }

    public static MPObjective addObjective(MPSolver solver,
    int m, int n, int r,
    int[][] board,
    Rectangle[] rectangles,
    MPVariable[][][] x_ijr,
    MPVariable[][] y_ij,
    Map<Integer, ArrayList<int[]>> A_r,
    Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr) {
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

     public static void addConstraints(
            MPSolver solver,
            int m, int n, int r,
            int[][] board,
            Rectangle[] rectangles,
            MPVariable[][][] x_ijr,
            MPVariable[][] y_ij,
            Map<Integer, ArrayList<int[]>> A_r,
            Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr) {
                printDebugInfo(A_r, B_ijr);
                double infinity = java.lang.Double.POSITIVE_INFINITY;
                for (int k = 0; k < r; k++) {
                    MPConstraint constraint = solver.makeConstraint(-infinity, 1, "");
                    for (int[] pos : A_r.get(k)) {
                        constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1);
                    }
                }

                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < n; j++) {
                        if (board[i][j] > 0) {
                            MPConstraint constraint = solver.makeConstraint(-infinity, 0, "");
                            constraint.setCoefficient(y_ij[i][j], 1);
                            for (int k = 0; k < r; k++) {
                                Map<int[], ArrayList<int[]>> posMap = B_ijr.get(k);
                                ArrayList<int[]> coveringPositions = posMap.getOrDefault(new int[] {i, j}, new ArrayList<>());
                                for (int[] pos : coveringPositions) {
                                    constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1);
                                }
                            }
                        } else if (board[i][j] < 0) {
                            MPConstraint constraint = solver.makeConstraint(0, infinity, "");
                            constraint.setCoefficient(y_ij[i][j], r);
                            for (int k = 0; k < r; k++) {
                                Map<int[], ArrayList<int[]>> posMap = B_ijr.get(k);
                                ArrayList<int[]> coveringPositions = posMap.getOrDefault(new int[]{ i, j }, new ArrayList<>());
                                for (int[] pos : coveringPositions) {
                                    constraint.setCoefficient(x_ijr[pos[0]][pos[1]][k], 1);
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
        Scanner sc = new Scanner(System.in);
        try {
            String path = "src\\dataset\\test1\\debug.txt";
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

        MPSolver solver = MPSolver.createSolver("CBC");
        if (solver == null) {
            System.out.println("Cannot create solver");
            return;
        }

        MPVariable[][][] x_ijr = new MPVariable[m][n][r];
        MPVariable[][] y_ij = new MPVariable[m][n];

        Map<Integer, ArrayList<int[]>> A_r = create_A_r(m, n, r, rectangles);
        Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr = create_B_ijr(m, n, r, rectangles, A_r);

        printDebugInfo(A_r, B_ijr);

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
        printProblemDetails(solver, m, n, r, board, rectangles, x_ijr, y_ij, A_r, B_ijr);
        System.out.println("Number of variable: " + solver.numVariables());
        System.out.println("Number of constraints: " + solver.numConstraints());
        
        final MPSolver.ResultStatus resultStatus = solver.solve();
        
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
            /* 
            System.out.println();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < r; k++) {
                        System.out.print(x_ijr[i][j][k]);
                    }
                    System.out.println();
                }
                System.out.println();
            }
            */
        }
        
    }
}
