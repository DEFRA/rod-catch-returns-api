@Library('defra-shared') _

pipeline {
    agent any
    stages {
        stage('Preparation') {
            steps {
                script {
                    git 'https://github.com/DEFRA/rod-catch-returns-api.git'
                    buildTag = generateBuildTag()
                    currentBuild.displayName = "${buildTag}"
                    jarFileName = "rcr_api-${buildTag}.jar"
                    targetRepo = "rcr-snapshots"
                    stageDir = "${WORKSPACE}/target/dist"
                    distFile = "${WORKSPACE}/target/rcr_api-${buildTag}.tgz"

                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh  """
                        printenv
                        ./mvnw versions:set -DnewVersion=${buildTag}
                        ./mvnw  -T 1C -B --update-snapshots -DskipTests -Ddependency-check.skip=true -DskipTests=true -Dcheckstyle.skip=true clean package
                    """
                }
            }
        }
        stage('Create distribution') {
            steps {
                script {
                    sh  """
                        mkdir ${stageDir}
                        cp target/${jarFileName} ${stageDir}/rcr_api.jar
                        cd ${stageDir} && tar cvzf ${distFile} * && cd -
                    """
                }
            }
        }
        stage('Archive distribution') {
            steps {
                script {
                    def server = Artifactory.server 'defra-artifactory'

                    def buildInfo = Artifactory.newBuildInfo()
                    buildInfo.name = 'rcr_api'
                    buildInfo.number = "${buildTag}"
                    buildInfo.env.capture = true
                    buildInfo.env.collect()

                    def uploadSpec = """{
                      "files": [
                        {
                          "pattern": "${distFile}",
                          "target": "${targetRepo}/api/"
                        }
                     ]
                    }"""

                    server.upload spec: uploadSpec, buildInfo: buildInfo
                    server.publishBuildInfo buildInfo
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
