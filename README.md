# DAPayCore

DAPay のコア機能を提供する Java プロジェクトです。

## 構成

- ビルドツール: Maven
- Java: 21
- テスト: JUnit 5

## ディレクトリ構成

```
DAPayCore/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/        # アプリケーションコード
│   │   └── resources/   # 設定ファイル等
│   └── test/
│       ├── java/        # テストコード
│       └── resources/
└── README.md
```

## ビルド & 実行

```bash
# ビルド
mvn clean package

# テスト
mvn test

# 実行
java -cp target/classes jp.co.dragonagency.dapaycore.App
```
