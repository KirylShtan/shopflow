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

  // Jenkins Windows service runs as SYSTEM → default ~/.m2 is under
  // C:\WINDOWS\system32\config\systemprofile and parallel builds hit AccessDenied.
  // Keep Maven cache in the job workspace instead.
  environment {
    MAVEN_REPO = "${env.WORKSPACE}\\.m2\\repository"
  }

  stages {
    stage('Test') {
      steps {
        bat '''
          @echo off
          set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
          for %%S in (catalog-service inventory-service order-service notification-service gateway-service) do (
            echo ========== TEST %%S ==========
            pushd %%S
            call mvnw.cmd -B clean test -Dmaven.repo.local=%MAVEN_REPO%
            if errorlevel 1 exit /b 1
            popd
          )
        '''
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Package') {
      steps {
        bat '''
          @echo off
          set "PATH=C:\\Windows\\System32;C:\\Windows\\System32\\WindowsPowerShell\\v1.0;%PATH%"
          for %%S in (catalog-service inventory-service order-service notification-service gateway-service) do (
            echo ========== PACKAGE %%S ==========
            pushd %%S
            call mvnw.cmd -B package -DskipTests -Dmaven.repo.local=%MAVEN_REPO%
            if errorlevel 1 exit /b 1
            popd
          )
        '''
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
