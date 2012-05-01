package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EmailAccessor extends MongoDB implements IAccessor {

    private Logger logger = Logger.getLogger(EmailAccessor.class);
    private OntModel model = null;
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[3];
    private List<String> queryEmails = new ArrayList<String>();

    public EmailAccessor(OntModel model) {
        super("test");
        this.model = model;
    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
        queryEmails.clear();
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {
            setFlags[0] = true;
            queryEmails.add(value);
        }
        else if (attribute.compareTo(attributes[1])==0) {
            setFlags[1] = true;
            queryEmails.add(value);
        }
        else if (attribute.compareTo(attributes[2])==0) {
            setFlags[2] = true;
            queryEmails.add(value);
        }
        else {
            throw new AccesorInitializationException("String value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
        }
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        return query();
    }

    private IResultSet query() {
        DBReader reader = this.startReader("emails");
        ResultSetImpl resultSet = new ResultSetImpl("Email Relations");

        BasicDBList emailPredicates = new BasicDBList();

        if ( !setFlags[0] && !setFlags[1] && !setFlags[2] ) {
            logger.info("No flags set. Empty return set");
            return resultSet;
        }

        BasicDBList predicates = new BasicDBList();
        for (String em: queryEmails) {
            Pattern emPattern = Pattern.compile(em, Pattern.CASE_INSENSITIVE);
            predicates.add(new BasicDBObject("To", emPattern));
            predicates.add(new BasicDBObject("From", emPattern));
            predicates.add(new BasicDBObject("CC", emPattern));
        }

        BasicDBObject keys = new BasicDBObject();
        keys.put("To", 1);
        keys.put("CC", 1);
        keys.put("From", 1);
        keys.put("Subject", 1);
        keys.put("_id", 0);

        BasicDBObject queryObject = new BasicDBObject("$or", predicates);
        reader.query(queryObject, keys);

        logger.info("Query: " + queryObject);

        int c = 1;
        String to, from, cc;
        while (reader.hasNext()) {
            BasicDBObject obj = (BasicDBObject) reader.next();
            List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
            to = obj.getString("To");
            if (to != null) entries.addAll(Utils.parseEmailAddresses(to));

            from = obj.getString("From");
            if (from != null) entries.addAll(Utils.parseEmailAddresses(from));

            cc = obj.getString("CC");
            if (cc != null)entries.addAll(Utils.parseEmailAddresses(cc));

            List<Individual> persons = new ArrayList<Individual>();

            for (Map.Entry<String, String> entry: entries)
                persons.add(Utils.createPersonFromNameEmail(entry.getKey(), entry.getValue(), model));

            resultSet.addResult(persons);
            logger.info(c + ". " + obj.toString()); c++;
        }

        return resultSet;
    }

    public void execute(String[] emails) {
        setFlags[0] = true;
        setFlags[1] = true;
        setFlags[2] = true;
        Collections.addAll(queryEmails, emails);
        query();
    }

    private class ResultSetImpl implements IResultSet {
        private String result;
        private ResultIterator resultIterator = new ResultIterator(model);

        public ResultSetImpl (String result) {this.result = result;}

        public void addResult(List<Individual> individuals) {
            this.resultIterator.add(individuals);
        }

        @Override
        public String printResults() {
            return result;
        }

        @Override
        public IResultIterator iterator() {
            return resultIterator;
        }
    }
}
