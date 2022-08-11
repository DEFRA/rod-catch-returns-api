@Library('defra-shared@master') _
def arti = defraArtifactory()
def s3

pipeline {
    agent any
    stages {
        stage('Preparation') {
            steps {
                script {
                    BUILD_TAG = buildTag.updateJenkinsJob()
                    JAR_FILENAME = "rcr_api-${BUILD_TAG}.jar"
                    STAGE_DIR = "${WORKSPACE}/target/dist"
                    withCredentials([
                        [
                            $class: 'AmazonWebServicesCredentialsBinding', 
                            credentialsId: 'aps-rcr-user',
                            accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                            secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                        ]
                    ]) {
                        s3 = defraS3()
                    }
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
                    DIST_FILE = s3.createDistributionFile(STAGE_DIR, "rcr_api")
                }
            }
        }
        stage('Upload distribution') {
            steps {
                script {
                    arti.uploadArtifact("rcr-snapshots/api/", "rcr_api", BUILD_TAG, DIST_FILE)
                    s3.uploadArtifact("rcr-snapshots/api/", "rcr_api", BUILD_TAG, DIST_FILE)
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
