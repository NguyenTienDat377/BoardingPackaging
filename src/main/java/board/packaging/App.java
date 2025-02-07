package board.packaging;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.modelbuilder.LinearExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        Scanner sc = new Scanner(System.in);
        System.out.println( "Hello World!" );
        int m = sc.nextInt();
        int n = sc.nextInt();
        int r = sc.nextInt();

        int[][] board = new int[m][n];
        int[] rectangles = new int[r];

        Map<Integer, ArrayList<int[]>> A_r = new HashMap<>();
        Map<Integer, Map<int[], ArrayList<int[]>>> B_ijr = new HashMap<>();

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = sc.nextInt();
            }
        }
        for (int i = 0; i < r; i++) {
            rectangles[i] = sc.nextInt();
        }

        MPSolver solver = MPSolver.createSolver("BoardPackaging");
        if (solver == null) {
            System.out.println("Cannot create solver");
            return;
        }

        int[][][] x_ijr = new int[m][n][r];
        int[][] y_ijr = new int[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < r; k++) {
                    x_ijr[i][j][k] = 0;
                }
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                y_ijr[i][j] = 0;
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] > 0) {}
            }
        }
    }
}
