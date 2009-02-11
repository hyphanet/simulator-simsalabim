package simsalabim;
import java.util.Vector;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;

public class CDataAggregator extends DataAggregator {

    private Vector<Number> datac = new Vector<Number>();

    private PrintStream out;
    private String fname;

    public CDataAggregator(String name) {
        super(name);
        out = System.out;
    }

    public CDataAggregator(String name, String fname) throws ScriptException {
        super(name);
        this.fname = fname;
    }


    public void next(long data) {
        datac.add(new Long(data));
    }

    public void next(double data) {
        datac.add(new Double(data));
    }

    public Long[] getLongData() {
        Long[] r = new Long[datac.size()];
        datac.copyInto(r);
        return r;
    }

    public Double[] getDoubleData() {
        Double[] r = new Double[datac.size()];
        datac.copyInto(r);
        return r;
    }

    public void complete() {
        PrintStream ps;
        
        if (fname != null) {
            try {
                ps = new PrintStream(new FileOutputStream(fname));
            } catch (IOException e) {
                throw new SimulationException("File error : " + e);
            }
        } else {
            ps = this.out;
        }

        print(ps);

        if (fname != null)
            ps.close();
    }

    public void print(PrintStream ps) {
        ps.println("#Created by Simsalabim. Data: " + name());
        ps.println("#name: " + name());
        ps.println("#type: matrix");
        ps.println("#rows: " + datac.size());
        ps.println("#columns: 1");
        for (Enumeration e = datac.elements() ; e.hasMoreElements() ;) {
            ps.println(e.nextElement().toString());
        }
    }
    
    public void print(PrintWriter pw) {
        pw.println("#Created by Simsalabim. Data: " + name());
        pw.println("#name: " + name());
        pw.println("#type: matrix");
        pw.println("#rows: " + datac.size());
        pw.println("#columns: 1");
       
        for (Enumeration e = datac.elements() ; e.hasMoreElements() ;) {
            pw.println(e.nextElement().toString());
        }
    }
    


}
