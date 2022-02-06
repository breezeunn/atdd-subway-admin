package nextstep.subway.line;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.CommonMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

public class SectionAcceptanceTest extends AcceptanceTest {

    private static final String SECTION_DISTANCE_EXCEEDED_EXCEPTION = "역 사이에 추가하려는 구간의 거리는 원래 구간 거리보다 작아야 합니다.";
    private static final String SECTION_ALREADY_EXIST_IN_THE_LINE_EXCEPTION = "등록하려는 구간이 이미 노선에 존재합니다.";
    private static final String STATIONS_NOT_EXIST_IN_THE_LINE_EXCEPTION = "상행역과 하행역 둘중 하나는 노선에 존재해야 합니다.";

    private static final String URL = "/lines";

    private Long startStationID;
    private Long endStationId;
    private int originalDistance = 10;
    private Long lineId;

    @BeforeEach
    void stationAndLineSetUp() {
        startStationID = StationAcceptanceTest
            .createStation("신도림역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();

        endStationId = StationAcceptanceTest
            .createStation("잠실역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();
        originalDistance = 10;
        lineId = getIdWithResponse(LineAcceptanceTest
            .createLine("2호선",
                "green",
                startStationID,
                endStationId,
                originalDistance)
        );
    }

    @DisplayName("노선 사이에 새로운 지하철역을 추가한다.")
    @Test
    void add_station_to_line() {
        // given
        // 지하철_노선에_추가할_중간역을_등록
        Long middleStationId = StationAcceptanceTest
            .createStation("신촌역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();
        // when
        // 지하철_노선에_지하철역_등록_요청
        ExtractableResponse<Response> response = addSection(startStationID, middleStationId, 4);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("노선에 상행 종점역을 추가한다.")
    @Test
    void add_first_station_to_line() {
        // given
        // 지하철_노선에_추가할_상행_종점을_등록
        Long firstStationId = StationAcceptanceTest
            .createStation("까치산역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();

        // when
        // 지하철_노선에_지하철역_등록_요청
        ExtractableResponse<Response> response = addSection(firstStationId, startStationID, 5);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("노선에 하행 종점역을 추가한다.")
    @Test
    void add_final_station_to_line() {
        // given
        // 지하철_노선에_추가할_하행_종점을_등록
        Long lastStationId = StationAcceptanceTest
            .createStation("신설동역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();

        // when
        // 지하철_노선에_지하철역_등록_요청
        ExtractableResponse<Response> response = addSection(endStationId, lastStationId, 7);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("원래 총 거리보다 크거나 같은 구간은 노선의 중간역으로 추가될 수 없다.")
    @ParameterizedTest
    @ValueSource(ints = {10, 15, 19})
    void section_with_equal_to_or_greater_than_original_distance_is_invalid(int distance) {
        // given
        // 지하철_노선에_추가할_역_등록
        Long middleStationId = StationAcceptanceTest
            .createStation("신촌역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();

        // when
        // 지하철_노선에_지하철역_등록_요청
        ExtractableResponse<Response> response = addSection(middleStationId, endStationId, distance);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(CommonMethod.getError(response).getMessage()).isEqualTo(
            SECTION_DISTANCE_EXCEEDED_EXCEPTION);
    }

    @DisplayName("이미 노선에 등록된 상행역과 하행역으로 이루어진 구간은 노선에 추가될 수 없다.")
    @Test
    void section_with_stations_already_in_the_line_is_invalid() {
        // when
        // 지하철_노선에_지하철역_등록_요청
        ExtractableResponse<Response> response = addSection(startStationID, endStationId, 3);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(CommonMethod.getError(response).getMessage()).isEqualTo(
            SECTION_ALREADY_EXIST_IN_THE_LINE_EXCEPTION);
    }

    @DisplayName("상행역과 하행역 모두 노선에 없는 역으로 이루어진 구간은 노선에 추가될 수 없다.")
    @Test
    void section_with_stations_not_in_the_line_is_invalid() {
        // given
        // 지하철_노선에_없는역_등록
        Long firstStationId = StationAcceptanceTest
            .createStation("까치산역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();
        Long lastStationId = StationAcceptanceTest
            .createStation("신설동역")
            .jsonPath()
            .getObject(".", StationResponse.class)
            .getId();
        // when
        // 지하철_노선에_지하철역_등록_요청
        ExtractableResponse<Response> response = addSection(firstStationId, lastStationId, 10);

        // then
        // 지하철_노선에_지하철역_등록됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(CommonMethod.getError(response).getMessage()).isEqualTo(
            STATIONS_NOT_EXIST_IN_THE_LINE_EXCEPTION);
    }


    private ExtractableResponse<Response> addSection(Long upStationId, Long downStationId,
        int distance) {
        Map<String, Object> params = body(upStationId, downStationId, distance);
        return CommonMethod.create(params, URL + "/" + lineId + "/sections");
    }

    private Map<String, Object> body(Long upStationId, Long downStationId, int distance) {
        Map<String, Object> params = new HashMap<>();
        params.put("upStationId", upStationId);
        params.put("downStationId", downStationId);
        params.put("distance", distance);
        return params;
    }

    private Long getIdWithResponse(ExtractableResponse<Response> response) {
        return response.jsonPath().getObject(".", LineResponse.class).getId();
    }

}