/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package batch.performance.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.data.CategoryDataset;
import org.xml.sax.InputSource;

import batch.performance.visualizer.om.DataSeries;
import batch.performance.visualizer.om.Result;
import batch.performance.visualizer.om.ScenarioResult;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Visualizer {
    
    private static final int WIDTH  = 200;
    private static final int HEIGHT = 100;
    
    public static void main(String[] args) throws Exception {
        if( args.length!=2 ) {
            System.out.println("Usage: Visualizer <result.xml> <outDir>");
            System.exit(-1);
        }
        
        File input = new File(args[0]);
        File outDir = new File(args[1]);
        
        // TODO: validation of the ipnut
        
    	// parse the test result
        Document dom = new SAXReader().read(new InputSource(input.toURL().toExternalForm()));
        Result r = new Result(dom);
        
        // generate per-scenario results
        for( Iterator itr=r.scenarios(); itr.hasNext(); ) {
            ScenarioResult scenario = (ScenarioResult)itr.next();

            DataSetBuilder dsb = new DataSetBuilder();
            
            for( Iterator jtr=scenario.dataSeries(); jtr.hasNext(); ) {
            	DataSeries ds = (DataSeries)jtr.next();
            	ds.addTo(dsb);
            }
            
            CategoryDataset ds = dsb.build();
            
            // full version
            printChart(
                createChart(ds,"Summary of "+scenario, true),
                new File(outDir,scenario.name+".large.png"), WIDTH*4, HEIGHT*4 );
            // small version
            printChart(
                createChart(ds,null,false),
                new File(outDir,scenario.name+".small.png"), WIDTH, HEIGHT );
        }
        
        {// generate the total summary chart
            DataSetBuilder dsb = new DataSetBuilder();
            for( Iterator itr=r.scenarios(); itr.hasNext(); ) {
                ScenarioResult scenario = (ScenarioResult)itr.next();
                scenario.addTo(dsb);
            }
            CategoryDataset dataset = dsb.build();
            printChart(
                createChart(dataset,"Summary", true),
                new File(outDir,"summary.large.png"), WIDTH*4, HEIGHT*4 );
            printChart(
                createChart(dataset,null,false),
                new File(outDir,"summary.small.png"), WIDTH, HEIGHT );
        }
    }
    
    private static void printChart( JFreeChart chart, File out, int width, int height ) throws IOException {
//      StandardLegend legend = (StandardLegend) chart.getLegend();
//      legend.setDisplaySeriesShapes(true);

      chart.setBackgroundPaint(new Color(0xFF, 0xFF, 0xFF));

      CategoryPlot plot = chart.getCategoryPlot();
      LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
        
      renderer.setItemLabelsVisible(true);
      renderer.setDrawShapes(false);
      renderer.setStroke(new BasicStroke(2));

      {// don't make the Y-axis too large
          // this will force the range to be recalculated
          plot.getRangeAxis().setAutoRange(false);
          plot.getRangeAxis().setAutoRange(true);
          // then set the maximum
          double mx = plot.getRangeAxis().getMaximumAxisValue();
          if( mx > 200 )
              plot.getRangeAxis().setMaximumAxisValue(200);
      }
      
      BufferedImage image = chart.createBufferedImage(width,height);
        
      ImageIO.write( image, "PNG", out );
    }
    
    private static JFreeChart createChart( CategoryDataset dataSet, String title, boolean detailed ) throws Exception {
        JFreeChart c = ChartFactory.createLineChart(
                title,
                null,
                null,
                dataSet,
                PlotOrientation.VERTICAL,  // orientation
                detailed,                   // don't draw the legend if not detailed.
                false,                     // tooltips
                false                      // urls
        );
        if( !detailed ) {
            // don't even draw the axices.
            c.getCategoryPlot().getRangeAxis().setVisible(false);
            c.getCategoryPlot().getDomainAxis().setVisible(false);
        }
        
        return c;
    }
}
