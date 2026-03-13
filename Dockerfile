# ビルド用ステージ
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Maven Wrapper などを含めてプロジェクト一式をコピー
COPY . .

# テストは一旦スキップして jar をビルド
RUN ./mvnw clean package -DskipTests

# 実行用ステージ（軽量な JRE イメージ）
FROM eclipse-temurin:21-jre
WORKDIR /app

# ビルド成果物の jar をコピー（名前は実際の jar に合わせる）
COPY --from=build /app/target/TokyoDisasterRiskVisualizationApp-0.0.1-SNAPSHOT.jar app.jar

# Spring Boot のデフォルトポート
EXPOSE 8080

# アプリ起動コマンド
ENTRYPOINT ["java","-jar","/app/app.jar"]