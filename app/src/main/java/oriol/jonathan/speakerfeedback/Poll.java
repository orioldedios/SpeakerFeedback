package oriol.jonathan.speakerfeedback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Poll {

    private String question;
    private List<String> options;
    private boolean isOpen;
    private Date start, end;
    private List<Integer> results;
    public int lastVote = -1;

    Poll(){}

    Poll(String question)
    {
            this.question = question;
            this.options = new ArrayList<>();
            this.options.add("yes");
            this.options.add("no");
            this.isOpen = true;
            this.start = new Date();
    }

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }
    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean open) {
        isOpen = open;
    }

    public Date getStart() {
        return start;
    }
    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }
    public void setEnd(Date end) {
        this.end = end;
    }

    public List<Integer> getResults() {
        return results;
    }

    public void addVote(int choice)
    {
        if(results != null && results.get(choice) != null)
        {
            lastVote = choice;
            results.set(choice,results.get(choice)+1);
        }
    }

    public void resetVotes()
    {
        results = new ArrayList<>();
        for (int i =0 ; i< options.size();i++)
        {
            results.add(0);
        }
    }

    //Simply make -= 1 to the last option voted
    public void undoLastVote()
    {
        if(results != null && lastVote != -1 && results.get(lastVote) != null)
        {
            results.set(lastVote, results.get(lastVote)-1);
            lastVote = -1;
        }
    }

    @Override
    public String toString() {

        String ret = question;

        for(String string : options)
        {
            question += "\n" + string;
        }

        return question;
    }
}
