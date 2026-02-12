import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.app.domain.RiskArea;
import com.example.app.domain.RiskLevel;
import com.example.app.domain.area.GeoPoint;
import com.example.app.service.EarthquakeRiskService;

class EarthquakeRiskServiceTest {

	@Test
  void testRiskLevelPriority() {

      // 東京駅付近のHIGHポリゴン
      RiskArea highArea = new RiskArea(
              List.of(
                      new GeoPoint(35.680, 139.765),
                      new GeoPoint(35.680, 139.770),
                      new GeoPoint(35.685, 139.770),
                      new GeoPoint(35.685, 139.765)
              ),
              RiskLevel.HIGH
      );

      // 八王子駅付近のMEDIUMポリゴン
      RiskArea mediumArea = new RiskArea(
              List.of(
                      new GeoPoint(35.650, 139.320),
                      new GeoPoint(35.650, 139.330),
                      new GeoPoint(35.660, 139.330),
                      new GeoPoint(35.660, 139.320)
              ),
              RiskLevel.MEDIUM
      );

      List<RiskArea> areas = List.of(highArea, mediumArea);
      EarthquakeRiskService service = new EarthquakeRiskService(areas);

      // 高リスク判定（東京駅）
      GeoPoint tokyoStation = new GeoPoint(35.682, 139.767);
      assertEquals(RiskLevel.HIGH, service.getRiskLevel(tokyoStation),
              "東京駅付近はHIGHであること");

      // 中リスク判定（八王子駅）
      GeoPoint hachiojiStation = new GeoPoint(35.655, 139.325);
      assertEquals(RiskLevel.MEDIUM, service.getRiskLevel(hachiojiStation),
              "八王子駅付近はMEDIUMであること");

      // ポリゴン外 → LOW
      GeoPoint outsidePoint = new GeoPoint(35.700, 139.780);
      assertEquals(RiskLevel.LOW, service.getRiskLevel(outsidePoint),
              "ポリゴン外はLOWであること");

      // 複数ポリゴン重複 → HIGH優先
      RiskArea overlapMedium = new RiskArea(
              List.of(
                      new GeoPoint(35.682, 139.765),
                      new GeoPoint(35.682, 139.770),
                      new GeoPoint(35.684, 139.770),
                      new GeoPoint(35.684, 139.765)
              ),
              RiskLevel.MEDIUM
      );
      EarthquakeRiskService serviceOverlap = new EarthquakeRiskService(
              List.of(highArea, overlapMedium)
      );
      GeoPoint overlapPoint = new GeoPoint(35.683, 139.767);
      assertEquals(RiskLevel.HIGH, serviceOverlap.getRiskLevel(overlapPoint),
              "重複ポリゴン内はHIGH優先であること");
  }

}
