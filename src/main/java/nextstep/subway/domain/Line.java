package nextstep.subway.domain;

import javax.persistence.*;

@Entity
public class Line extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_line_to_up_station"))
    private Station upStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_line_to_down_station"))
    private Station downStation;

    private Long distance = 0L;

    public Line() {

    }

    public Line(String name, String color, Station upStation, Station downStation, long distance) {
        this.name = name;
        this.color = color;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public Long getDistance() {
        return distance;
    }
}