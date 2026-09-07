pipeline {
  agent any

  tools {
    jdk 'jdk21'
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  stages {
    stage('Test') {
      parallel {
        stage('catalog-service') {
          steps {
            dir('catalog-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B clean test
              '''
            }
          }
        }
        stage('inventory-service') {
          steps {
            dir('inventory-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B clean test
              '''
            }
          }
        }
        stage('order-service') {
          steps {
            dir('order-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B clean test
              '''
            }
          }
        }
        stage('notification-service') {
          steps {
            dir('notification-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B clean test
              '''
            }
          }
        }
        stage('gateway-service') {
          steps {
            dir('gateway-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B clean test
              '''
            }
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Package') {
      parallel {
        stage('catalog-service') {
          steps {
            dir('catalog-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B package -DskipTests
              '''
            }
          }
        }
        stage('inventory-service') {
          steps {
            dir('inventory-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B package -DskipTests
              '''
            }
          }
        }
        stage('order-service') {
          steps {
            dir('order-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B package -DskipTests
              '''
            }
          }
        }
        stage('notification-service') {
          steps {
            dir('notification-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B package -DskipTests
              '''
            }
          }
        }
        stage('gateway-service') {
          steps {
            dir('gateway-service') {
              bat '''
                @echo off
                set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
                call mvnw.cmd -B package -DskipTests
              '''
            }
          }
        }
      }
    }
  }

  post {
    success {
      archiveArtifacts artifacts: '*/target/*.jar', fingerprint: true, allowEmptyArchive: true
    }
    failure {
      echo 'Pipeline failed — check Test stage surefire reports / console.'
    }
  }
}
