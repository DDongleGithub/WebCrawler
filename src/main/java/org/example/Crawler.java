package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    private HashSet<String> urlLink;
    private int MAX_DEPTH = 2;
    public Connection connection;
    public Crawler(){
        //set up the connection to MySql
        connection = DatabaseConnection.getConnection();
        urlLink = new HashSet<String>();
    }

    public void getPageTextAndLinks(String url, int depth){
        if(!urlLink.contains(url)){
            if(urlLink.add(url)){
                System.out.println(url);
            }
            try {
                //parsing HTML object to java Document object
                Document document = Jsoup.connect(url).timeout(5000).get();
                //get text from document object
                String text = document.text().length()<501? document.text(): document.text().substring(0,500);
                System.out.println(text);
                //Insert data into pages table
                PreparedStatement preparedStatement = connection.prepareStatement("insert into pages values(?,?,?)");
                preparedStatement.setString(1, document.title());
                preparedStatement.setString(2,url);
                preparedStatement.setString(3, text);
                preparedStatement.executeUpdate();
                //Increase depth
                depth++;
                //if depth is greater than max return
                if(depth > MAX_DEPTH){
                    return;
                }
                // get hyperlink available on the current page
                Elements availableLinksOnPage = document.select("a[href]");
                for(Element currentLink : availableLinksOnPage){
                    //run method recursively for every link available on the page
                    getPageTextAndLinks(currentLink.attr("abs:href"),depth);

                }
            }catch(IOException ioException){
                ioException.printStackTrace();
            }
            catch (SQLException sqlException){
                sqlException.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.getPageTextAndLinks("https://www.javatpoint.com/",0);
    }
}