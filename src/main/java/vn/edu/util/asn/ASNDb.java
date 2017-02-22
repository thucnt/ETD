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
    
    public static void getPaperCitationByYear(int year, String outputFile){
        BufferedWriter outStream = null;
        try {
            outStream = new BufferedWriter(new FileWriter(outputFile));
            Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
            PreparedStatement papers = con.prepareStatement(sqlPapersByYear);
            papers.setInt(1, year);
            ResultSet rs1 = papers.executeQuery();
            PreparedStatement refPapers = con.prepareStatement(sqlPaperRef);
            ResultSet rs2 = null;
            while (rs1.next()){
                int id = rs1.getInt("idPaper");
                refPapers.setInt(1, id);
                rs2 = refPapers.executeQuery();
                StringBuilder outputLine = new StringBuilder();
                outputLine.append(id);
                outputLine.append(";");
                while (rs2.next()){
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
        }
        finally{
            try {
                outStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ASNDb.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public static void main(String[] args){
        getPaperCitationByYear(2005,"/Users/thucnt/git/ETD/data/papers2005.txt");
    }
}
