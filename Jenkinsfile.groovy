pipeline {

  options {
    buildDiscarder(
      logRotator(
        numToKeepStr: '30',
        daysToKeepStr: '30',
        artifactNumToKeepStr: '30',
        artifactDaysToKeepStr: '30'
      )
    )
    quietPeriod 30
    disableConcurrentBuilds()
  }

  triggers {
    cron('H 0 */3 * *')
  }

  agent {
    label 'generic'				
  }

  stages {
    stage("Git Checkout") {
      steps {
        script {
          def git_repo = "github.company.com"     
          def git_credential_id = ""            
          git changelog: false, credentialsId: "${git_credential_id}", poll: false, url: "${git_repo}"
        }
      }
    }

    stage("Generic Build Name") {
      steps {
        script {
          def git_commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          def time_stamp = sh(script: "date +'%y%m%d-%H%M'", returnStdout: true).trim()
          version = "build-${time_stamp}-${git_commit}"
          currentBuild.displayName = version
        }
      }
    }

    stage("Execute script") {
      steps {
        script {
          def python_script_location = "folder/script.py"         
          script_exit_code = sh(script: "bash -l -c 'python ${python_script_location}'", returnStatus: true).toString().trim()
          if("${script_exit_code}" == "0") {
            echo "script executed successfully!"
            currentBuild.result = 'SUCCESS'
          } else {
            error("script execution failed!")
            currentBuild.result = 'FAILURE'
          }
        }
      }
    }
  }
}
