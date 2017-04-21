package info.kwarc.teaching.AI.Kalah.WS1617.agents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import info.kwarc.teaching.AI.Kalah.Agent;
import info.kwarc.teaching.AI.Kalah.Board;
import info.kwarc.teaching.AI.Kalah.Game;
import info.kwarc.teaching.AI.Kalah.RandomPlayer;
import info.kwarc.teaching.AI.Kalah.Tournament;
import scala.Int;


public class KalahBot extends info.kwarc.teaching.AI.Kalah.Agent{
    Board board;
    int naechsterZug=0;
    DudeBoard currentDudeBoard;
    int player;
    int zuege=0;
    
    @Override
    public void init(Board board, boolean playerOne) {
        List<Integer> houses = (List<Integer>)board.getState().copy$default$1();
        int seed = houses.get(0);
        if (playerOne){
            player=1;
        }else{
            player=-1;
        }
        currentDudeBoard = new DudeBoard(houses.size(),seed,player);
        this.board = board;
        //System.out.println("We are player+ " + player);
    }
    
    @Override
    public int move() {
        zuege++;
        int move=1;
        //MaxDepth for Iterative Deepening
        for (int i = 2;i<=12;i=i+2){
            currentDudeBoard.updateBoard(board, player);
            Minimax m1 = new Minimax(currentDudeBoard,i,player);
            if(player==1){
                move = m1.calculateMove(currentDudeBoard.boardState,player)+1;
            }else{
                move = m1.calculateMove(currentDudeBoard.boardState,player)-currentDudeBoard.houses;
            }
            
            this.timeoutMove_$eq(move);
        }
        return move;
        
    }
    
    @Override
    public String name() {
        return "Dude";
    }
    
    @Override
    public Iterable<String> students() {
        String foo[] = { "First Name, Last Name" };
        List<String> list = Arrays.asList(foo);
        return list;
    }
    
    
    /*---------------------------------------------------------------------------------------------
     * Hilfsstrukturen DudeBoard, MiniMax
     */
    
    public class DudeBoard {
        int player;
        int houses, seeds;
        ArrayList<Integer> boardState = new ArrayList<>();
        public DudeBoard(int houses, int seeds,int player){
            this.houses = houses;
            this.seeds = seeds;
            for (int i=0; i<houses;i++){
                boardState.add(seeds);
            }
            boardState.add(0);//Mulde
            for (int i=0; i<houses;i++){
                boardState.add(seeds);
            }
            boardState.add(0);//Mulde
            boardState.add(0); // Flag
            
        }
        
        public void updateBoard(Board board, int player){
            ArrayList<Integer> tmpState = new ArrayList<>();
            //System.out.println("Player"+player);
            this.player=player;
            
            
            List<Integer> houses1= (List<Integer>)board.getState().copy$default$1();
            List<Integer> houses2= (List<Integer>)board.getState().copy$default$2();
            //System.out.println(houses1);
            
            for (int i = 0; i<houses1.size(); i++){
                tmpState.add(houses1.get(i));
            }
            tmpState.add(0);
            for (int i = 0; i<houses2.size(); i++){
                tmpState.add(houses2.get(i));
            }
            int score1 = board.getScore(1);
            tmpState.set(houses1.size(), score1);
            int score2 = board.getScore(2);
            
            tmpState.add(score2);
            tmpState.add(0);//Flag
            
            boardState = tmpState;
        }
        
        //Liste von Spieler 1 houses + mulde + Spieler2 houses + Mulde + nochmalZiehenFLAG
        
        //int player gibt spieler an
        
        public  ArrayList<Integer> makeMove(ArrayList<Integer> state, int house, int player){
            
            if(state.get(house) == 0){
                return null;
            }
            //Get stones in house
            int steps = state.get(house);
            state.set(house, 0);
            
            if(player==1) {
                int realSteps=(house + steps)%(houses*2+1);
                
                
                for (int i = 0; i < steps; i++) {
                    //Add stones in houses
                    if (houses * 2 + 1 == (house+i+1) % (houses*2+2)){
                        steps++;
                        continue;
                    }
                    state.set((house + i+1) % (houses * 2 + 2), state.get((house + i+1) % (houses * 2 + 2)) + 1);
                }
                //Sonderfaelle. Letzter Stein in Mulde, dann nochmal neu ziehen
                int abstand = (houses*2-realSteps);
                if (realSteps == houses) {
                    
                    state.set(houses*2+2, Integer.MAX_VALUE);
                }
                
                else if (((house + steps)%(houses*2+2))< houses) {
                    
                    //-1 weil wir oben schon einen wenn dann reingelegt haben
                    if (((state.get((house + steps)%(houses*2+2)))-1 == 0)&&(state.get(houses*2-((house + steps)%(houses*2+2)))!=0)) {//Gegner mulde darf nicht leer sein
                        int gegnerMurmeln = state.get(houses*2-((house + steps)%(houses*2+2)));
                        state.set(houses, state.get(houses) + gegnerMurmeln+1);
                        //murmeln bei dir in die mulde tun
                        state.set(((house + steps)%(houses*2+2)),0);//murmeln bei dir weg
                        state.set(houses*2-((house + steps)%(houses*2+2)),0);//murmeln beim Gegner weg
                    }
                }
            }
            //Player 2
            else {
                int realSteps=(house + steps)%(houses*2+1);
                for (int i = 0; i < steps; i++) {
                    //Add stones in houses, fuer Spieler 2. Wenn in Gewinnmulde
                    if (houses == ((house+i+1) % (houses*2+2))){
                        steps++;
                        continue;
                    }
                    state.set((house + i+1) % (houses * 2 + 2), state.get(((house + i+1) % (houses * 2 + 2))) + 1);
                }
                //Sonderfaelle. Letzter Stein in Mulde, dann nochmal neu ziehen
                int abstand = (realSteps-houses)-1;
                if ((realSteps) == houses*2+1) {
                    
                    state.set(houses*2+2,Integer.MAX_VALUE);
                }
                else if (((house + steps)% (houses*2+2)) > houses && ((house + steps)% (houses*2+2)) < houses*2+1) {
                    
                    if ((state.get(((house + steps)% (houses*2+2)))-1 <= 0) && (state.get(houses*2-((house + steps)% (houses*2+2)))!=0)) {//Gegner mulde darf nicht leer sein
                        
                        int gegnerMurmeln = state.get(houses*2-((house + steps)% (houses*2+2)));
                        
                        state.set(houses*2+1, state.get(houses*2+1) + gegnerMurmeln+1);//murmeln bei dir in die mulde tun
                        state.set(((house + steps)% (houses*2+2)),0);//murmeln bei dir weg
                        state.set(houses*2-((house + steps)% (houses*2+2)),0);//murmeln beim Gegner weg
                    }
                }
            }
            
            return state;
        }
    }
    
    public class Minimax {
        int player;
        int naechsterZug=0;
        int maxDepth;
        DudeFix.DudeBoard currentDudeBoard;
        public Minimax(DudeFix.DudeBoard d, int maxDepth,int player){
            this.player = player;
            currentDudeBoard = d;
            this.maxDepth = maxDepth;
        }
        
        public int calculateMove(ArrayList<Integer> state, int p){
            max(state,currentDudeBoard.player,maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE,0) ;
            return naechsterZug;
        }
        
        private int evaluate(ArrayList<Integer> state){
            int topf1=0;
            for(int i=0;i<=currentDudeBoard.houses;i++){
                topf1 = topf1 + state.get(i);
            }
            int topf2=0;
            for(int i=currentDudeBoard.houses+1;i<=currentDudeBoard.houses*2+1;i++){
                topf2 = topf2 + state.get(i);
            }
            if (currentDudeBoard.player==1){
                return topf1-topf2;
            }else{
                return topf2-topf1;
            }
            
        }
        
        private int evaluate2(ArrayList<Integer> state){
            if (currentDudeBoard.player==1){
                return state.get(currentDudeBoard.houses)-state.get(currentDudeBoard.houses*2+1);
            }else{
                return state.get(currentDudeBoard.houses*2+1)-state.get(currentDudeBoard.houses);
            }
            
        }
        
        public boolean muldeLeer1(ArrayList<Integer> state){
            for (int i = 0; i<currentDudeBoard.houses; i++){
                if (state.get(i)!=0){
                    return false;
                }
            }
            return true;
        }
        
        public boolean muldeLeer2(ArrayList<Integer> state){
            for (int i = currentDudeBoard.houses+1; i<currentDudeBoard.houses*2+1; i++){
                if (state.get(i)!=0){
                    return false;
                }
            }
            return true;
        }
        
        
        public int max(ArrayList<Integer> state, int player, int maxDepth, int alpha, int beta,int count){
            
            int from;
            int to;
            if (player==1){
                from=0;
                to=currentDudeBoard.houses;
            }else{
                from=currentDudeBoard.houses+1;
                to=currentDudeBoard.houses*2+1;
            }
            if(muldeLeer1(state) || muldeLeer2(state)){
                
                return evaluate(state);
            }
            
            if(maxDepth==0){
                
                return evaluate2(state);
            }
            int max = alpha;
            for(int i=from;i<to;i++){
                ArrayList<Integer> copy = new ArrayList<>(state);
                if(currentDudeBoard.makeMove(state,i,player)==null){
                    continue;
                }
                int wert=Integer.MIN_VALUE;
                if(state.get(currentDudeBoard.houses*2+2) == Integer.MAX_VALUE){
                    //Nochmal, wenn Flag gesetzt
                    state.set(currentDudeBoard.houses*2+2,0);
                    wert = max(state, player, maxDepth-1, alpha,beta ,count+1);
                }else {
                    
                    wert = min(state, -player, maxDepth - 1, max, beta,count+1);
                }
                state = new ArrayList<>(copy);
                
                if(wert > max){
                    max = wert;
                    if(max >= beta){
                        break;
                    }
                    if(maxDepth==this.maxDepth){
                        naechsterZug = i;
                    }
                }
                
            }
            
            return max;
        }
        
        
        public int min(ArrayList<Integer> state, int player, int maxDepth, int alpha, int beta,int count){
            int from;
            int to;
            if (player==-1){
                
                from=currentDudeBoard.houses+1;
                to=currentDudeBoard.houses*2+1;
            }else{
                from=0;
                to=currentDudeBoard.houses;
            }
            if(muldeLeer2(state) || muldeLeer1(state)){
                return evaluate(state);
            }
            if(maxDepth==0){
                return evaluate2(state);
            }
            int min = beta;
            
            for(int i=from;i<to;i++){	     
                ArrayList<Integer>copy = new ArrayList<>(state);
                if(currentDudeBoard.makeMove(state,i,player)==null){
                    continue;
                }
                state = new ArrayList<Integer>(copy);
                int wert=Integer.MAX_VALUE;
                if(currentDudeBoard.makeMove(state,i,player)==null){
                    continue;
                }
                if(state.get(currentDudeBoard.houses*2+2) == Integer.MAX_VALUE) {
                    state.set(currentDudeBoard.houses*2+2,0);
                    wert = min(state, player, maxDepth - 1, alpha, beta,count+1);
                }else{
                    wert = max(state, -player, maxDepth - 1, alpha, min,count+1);
                }
                state = new ArrayList<Integer>(copy);
                if(wert < min){
                    min = wert;
                    if(min <= alpha){
                        break;
                    }
                }
            }
            return min;
        }
        
    }
}
