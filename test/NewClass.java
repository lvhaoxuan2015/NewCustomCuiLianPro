/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author pc
 */
public class NewClass {

    static String strs = "100, 100, 100, 90, 85, 80, 70, 60, 50, 45, 40, 30, 30, 25, 25, 20, 20, 10";

    public static void main(String[] args) {
        int i = 1;
        for (String str : strs.split(", ")) {
            System.out.println("        " + i++ + ": " + str);
        }
    }
}
