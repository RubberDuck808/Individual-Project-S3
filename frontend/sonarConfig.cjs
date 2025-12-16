require('dotenv').config();
const scannerModule = require('sonarqube-scanner');
const scanner = scannerModule.default;

scanner(
  {
    serverUrl: 'http://localhost:9000',
    options: {
      'sonar.projectKey': 'I548789_individual-project-s3_0ed3868f-3af8-4e41-a583-5ab0e0896d70',
      'sonar.projectName': 'Individual-Project-S3',
      'sonar.token': process.env.SONAR_TOKEN,
      'sonar.sources': 'src',
      'sonar.exclusions': '**/*.test.jsx,**/*.spec.jsx,coverage/**',
      'sonar.javascript.lcov.reportPaths': 'coverage/lcov.info',
      'sonar.sourceEncoding': 'UTF-8',
      'sonar.qualitygate.wait': true,
    },
  },
  () => {
    console.log('SonarQube scan finished');
    process.exit();
  }
);
