package server.model;

public class PlayerRank{

    private final String username;
    private final Integer totalScore;
    private Integer ranking;
    private final Integer gamesPlayed;

    public PlayerRank(String username, Integer totalScore, Integer gamesPlayed){
        this.username = username;
        this.totalScore = totalScore;
        this.gamesPlayed= gamesPlayed;
        this.ranking = 0;
    }

    public Integer getTotalScore() {return totalScore;}
    public Integer getGamesPlayed() {return gamesPlayed;}
    public Integer getRanking() {return ranking;}
    public String getUsername() {return username;}
    public void setRanking(Integer ranking) {this.ranking = ranking;}
}
