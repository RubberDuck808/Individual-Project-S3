require('dotenv').config();
const scannerModule = require('sonarqube-scanner');
const scanner = scannerModule.default;

// Prioritize environment variable over .env file
// This allows overriding .env with system environment variable
const sonarToken = process.env.SONAR_TOKEN;
const sonarHostUrl = process.env.SONAR_HOST_URL || 'http://localhost:9000';

if (!sonarToken) {
  console.error('ERROR: SONAR_TOKEN is not set in environment variable');
  console.error('Please set it with: $env:SONAR_TOKEN = "your-token"');
  process.exit(1);
}

scanner(
  {
    serverUrl: sonarHostUrl,
    options: {
      'sonar.projectKey': 'I548789_individual-project-s3_0ed3868f-3af8-4e41-a583-5ab0e0896d70:frontend',
      'sonar.projectName': 'Individual-Project-S3 (Frontend)',
      'sonar.token': sonarToken,
      'sonar.sources': 'src',
      'sonar.tests': 'tests,e2e',
      'sonar.test.inclusions': '**/*.test.jsx,**/*.spec.jsx,**/*.test.js,**/*.spec.js,**/*.spec.e2e.js,**/*.spec.e2e.ts,**/*.spec.ts',
      'sonar.exclusions': 'coverage/**,node_modules/**,playwright-report/**,test-results/**',
      'sonar.javascript.lcov.reportPaths': 'coverage/lcov.info',
      'sonar.sourceEncoding': 'UTF-8',
      'sonar.qualitygate.wait': false,
      'sonar.coverage.exclusions': '**/*.test.jsx,**/*.spec.jsx,**/*.test.js,**/*.spec.js,**/*.spec.ts,**/*.spec.e2e.*',
    },
  },
  () => {
    console.log('SonarQube scan finished');
    process.exit();
  }
);
