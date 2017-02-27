/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.edu.util.asn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class ASNDb {

    private static String dbUrl = "jdbc:mysql://localhost:3306/mas";
    private static String dbUser = "root";
    private static String dbPwd = "thuc1980";
    private static String sqlPapersByYear = "select distinct idPaper from paper where year = ?";
    private static String sqlPaperRef = "select idPaper, idPaperRef from paper_paper where idPaper = ?";

    public static Connection getDBConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
        } catch (SQLException ex) {
            Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    public static List<Integer> getRefListFromDB(int idPaper) {
        List<Integer> refList = new ArrayList<>();
        Connection con = getDBConnection();
        PreparedStatement refPapers;
        try {
            refPapers = con.prepareStatement(sqlPaperRef);
            refPapers.setInt(1, idPaper);
            ResultSet rs = refPapers.executeQuery();
            while (rs.next()) {
                refList.add(rs.getInt("idPaperRef"));
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return refList;
    }

    public static void getPaperCitationByYear(int year, String outputFile) {
        BufferedWriter outStream = null;
        try {
            outStream = new BufferedWriter(new FileWriter(outputFile));
            Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement papers = con.prepareStatement(sqlPapersByYear);
            papers.setInt(1, year);
            ResultSet rs1 = papers.executeQuery();
            PreparedStatement refPapers = con.prepareStatement(sqlPaperRef);
            ResultSet rs2 = null;
            while (rs1.next()) {
                int id = rs1.getInt("idPaper");
                refPapers.setInt(1, id);
                rs2 = refPapers.executeQuery();
                StringBuilder outputLine = new StringBuilder();
                outputLine.append(id);
                outputLine.append(";");
                while (rs2.next()) {
                    outputLine.append(rs2.getInt("idPaperRef"));
                    outputLine.append(",");
                }
                outputLine.deleteCharAt(outputLine.length() - 1);
                outStream.write(outputLine.toString());
                outStream.newLine();
                rs2.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static int[][] createMatrix() {
        int[][] matrix = new int[1000][];
        HashSet<Integer> idSet = new HashSet<>();
        HashMap<Integer, Integer> idMap = new HashMap<>();
        for (int i = 2; i <= 1001; i++) {
            List<Integer> list = getRefListFromDB(i);
            matrix[i - 2] = new int[list.size()];
            for (int j = 0; j < list.size(); j++) {
                matrix[i - 2][j] = list.get(j);
                idSet.add(list.get(j));
            }
        }
        int key = 1;
        for (Integer id : idSet) {
            idMap.put(key, id);
            key++;
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                for (Map.Entry<Integer, Integer> e : idMap.entrySet()) {
                    if (e.getValue() == matrix[i][j]) {
                        matrix[i][j] = e.getKey();
                    }
                }
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        //getPaperCitationByYear(2005,"/Users/thucnt/git/ETD/data/papers2005.txt");
        int[][] documents = createMatrix();
//        int key = 0;
//        for (Integer id : idSet){
//            key++;
//            idMap.put(key, id);
//        }
        System.out.println();
    }
}
