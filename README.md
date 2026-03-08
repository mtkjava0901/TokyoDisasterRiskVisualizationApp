# 東京都災害リスク可視化アプリ (バックエンド)

東京都の各種災害リスク（地震、水害等）を可視化するアプリケーションのバックエンドAPIシステムです。地図データやリスク等に関するデータをフロントエンドに提供します。

## 技術スタック
- **言語**: Java 21
- **フレームワーク**: Spring Boot (WebMVC, Validation, Thymeleaf)
- **ビルドツール**: Maven
- **ORM / データアクセス**: MyBatis
- **データベース**: MySQL

## ディレクトリ構成
```
src/main/java/com/example/app/
  ├── config/          # アプリケーション全体やセキュリティ等の設定クラス
  ├── controller/      # APIエンドポイントのリクエスト・レスポンス処理 (REST API)
  ├── domain/          # ビジネスロジックのコア、エンティティ
  ├── dto/             # Data Transfer Object (データのやり取り用クラス)
  ├── exception/       # カスタム例外やグローバルな例外ハンドリング処理
  ├── infrastructure/  # データベースアクセス(MyBatis Mapper)や外部通信処理
  └── service/         # ユースケース・ビジネスロジックの実装
```

## セットアップと起動方法

### 前提条件
- Java 21がインストールされていること
- MySQLが起動しており、必要なデータベース/スキーマが存在すること

### 起動手順

1. **環境設定**
   `src/main/resources/application.properties` または `application.yml` を確認し、データベースの接続情報（URL、ユーザー、パスワード）等が正しいか設定してください。

2. **依存関係の解決とビルド**
   Mavenを利用して依存関係をダウンロードします。
   ```bash
   ./mvnw clean install
   # Windowsの場合は mvnw.cmd clean install
   ```

3. **アプリケーションの起動**
   以下のコマンドでSpring Bootアプリケーションを起動します。
   ```bash
   ./mvnw spring-boot:run
   # Windowsの場合は mvnw.cmd spring-boot:run
   ```

起動後、指定のポート（デフォルトは `8080`）でAPIの待機が開始されます。フロントエンドから当該ポートに向けてリクエストを行ってください。
