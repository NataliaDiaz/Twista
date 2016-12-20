/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package daw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author User
 */
public class FirstServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("application/json");
        String NL = System.getProperty("line.separator");
        PrintWriter out = response.getWriter();
        try {
            String keyword = request.getParameter("keyword");
            String answer = TwitterSearch.tweetsJson(keyword);
           Lift lift = new Lift();
           lift.fire(answer);
           try{
            Set<String> pics = lift.getPicturesByKeyword(keyword);
            request.getSession().setAttribute("pics", pics);
           }catch(Exception e){ System.out.println("No pic found");}
           
//            lift.fire(answer);
            Set<String> set = lift.getSentimentAtDateByKeyword(keyword);

            List<String> list = new ArrayList<String>();
            list.add("2009-01-12,27,67,6");
            list.add("2008-12-08,27,67,6");
            list.add("2008-11-02,26,67,7");
            list.add("2008-10-05,29,66,5");
            list.add("2008-09-08,34,61,5");
            list.add("2008-08-18,31,64,5");
            list.add("2008-07-21,30,64,6");
            list.add("2008-06-09,28,66,6");
            list.add("2008-04-28,27,66,7");
            list.add("2008-03-10,32,63,5");
            list.add("2008-01-22,31,63,6");
            list.add("2007-12-17,34,60,6");
            list.add("2007-11-05,31,63,6");
            list.add("2007-09-10,33,61,6");
            list.add("2007-07-30,31,63,6");
            list.add("2007-06-11,29,66,5");
            list.add("2007-04-23,35,60,5");
            list.add("2007-03-05,35,60,5");
            list.add("2007-01-20,35,60,5");
            list.add("2006-12-11,34,61,5");
            list.add("2006-10-30,39,57,4");
            list.add("2006-10-16,38,57,5");
            list.add("2006-10-02,39,56,5");
            list.add("2006-09-11,42,53,5");
            list.add("2006-07-24,39,56,5");
            list.add("2006-06-12,37,58,5");
            list.add("2006-04-24,36,57,7");
            list.add("2006-03-13,37,58,5");
            list.add("2006-01-29,39,54,7");
            list.add("2005-12-12,39,55,6");
            list.add("2005-11-07,38,57,5");
            list.add("2005-10-10,39,54,7");
            list.add("2005-09-12,40,55,5");
            list.add("2005-07-11,46,49,5");
            list.add("2005-05-16,47,47,6");
            list.add("2005-04-03,48,46,6");
            list.add("2005-02-14,50,45,5");
            list.add("2005-01-17,50,44,6");
            list.add("2004-12-13,49,44,7");
            list.add("2004-10-31,49,48,3");
            list.add("2004-10-18,49,47,4");
            list.add("2004-09-19,47,48,5");
            list.add("2004-08-25,47,48,5");
            list.add("2004-07-21,48,46,6");
            list.add("2004-06-28,45,49,6");
            list.add("2004-05-03,47,46,7");
            list.add("2004-03-08,50,46,4");
            list.add("2004-01-12,54,41,6");
            list.add("2003-12-14,58,34,9");
            list.add("2003-12-13,52,41,7");
            list.add("2003-11-10,51,44,5");
            list.add("2003-09-22,49,45,6");
            list.add("2003-07-28,56,38,6");
            list.add("2003-05-19,62,31,7");
            list.add("2003-04-13,71,23,6");
            list.add("2003-03-30,66,29,5");
            list.add("2003-03-23,67,28,5");
            list.add("2003-03-17,62,33,5");
            list.add("2003-02-05,61,31,8");
            list.add("2003-01-21,54,40,6");
            list.add("2002-12-09,62,33,5");
            list.add("2002-10-21,63,31,6");
            list.add("2002-09-05,64,30,6");
            list.add("2002-07-21,67,27,6");
            list.add("2002-06-10,69,23,8");
            list.add("2002-05-18,75,18,7");
            list.add("2002-04-07,74,20,6");
            list.add("2002-01-21,82,13,5");
            list.add("2001-12-10,85,11,4");
            list.add("2001-11-11,88,7,5");
            list.add("2001-09-16,82,12,6");
            list.add("2001-06-25,50,35,15");
            list.add("2001-04-23,56,30,14");
            list.add("2001-03-04,57,22,21");
            BufferedReader br ;
            File results = new File(getServletContext().getRealPath("/")+"/twistaData.txt");
            BufferedWriter out2;
            try {
                    out2 = new BufferedWriter(new FileWriter(results));
                    for(String row:list)
                        out2.write(row+NL);
                    out2.flush();
                    out2.close();
            } catch (IOException ex) {
                    System.out.println("Error creating results file");
                    return;
            }
            int count=0;
            StringBuffer aggregated = new StringBuffer();
            for(String row1:set){
                if(count>0)
                    aggregated.append(", &nbsp; ");
                aggregated.append(row1);
            }
            request.getSession().setAttribute("aggregated", aggregated.toString());
            response.sendRedirect("TwitterSentimentAnalysis.jsp");
        }catch(Exception e){
            String msg = e.getMessage();
        }
        finally {
            out.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
