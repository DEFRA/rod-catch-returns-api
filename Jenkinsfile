@Library('defra-shared@master') _
def arti = new uk.gov.defra.jenkins.Artifactory(this, env.DEFRA_ARTIFACTORY_ID, env.DEFRA_ARTIFACTORY_CREDENTIALS_ID)

pipeline {
    agent any
    stages {
        stage('Preparation') {
            steps {
                script {
                    BUILD_TAG = buildTag.updateJenkinsJob()
                    JAR_FILENAME = "rcr_api-${BUILD_TAG}.jar"
                    STAGE_DIR = "${WORKSPACE}/target/dist"
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh  """
                        ./mvnw versions:set -DnewVersion=${BUILD_TAG}
                        ./mvnw  -T 1C -B --update-snapshots -Dcheckstyle.skip=true -Dfindbugs.skip=true -Ddependency-check.skip=true -DskipTests=true -Dcheckstyle.skip=true clean package
                    """
                }
            }
        }
        stage('Create distribution') {
            steps {
                script {
                    // Stage a distribution in the STAGE_DIR folder
                    sh  """
                        mkdir ${STAGE_DIR}
                        cp target/${JAR_FILENAME} ${STAGE_DIR}/rcr_api.jar
                    """
                }
            }
        }
        stage('Archive distribution') {
            steps {
                script {
                    DIST_FILE = arti.createDistributionFile(STAGE_DIR, "rcr_api")
                }
            }
        }
        stage('Upload distribution') {
            steps {
                script {
                    arti.uploadArtifact("rcr-snapshots/api/", "rcr_api", BUILD_TAG, DIST_FILE)
                }
            }
        }
    }
    post {
        cleanup {
            cleanWs cleanWhenFailure: false
        }
    }
}
