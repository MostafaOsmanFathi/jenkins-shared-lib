// Shared library step: buildAndPushImage
// Builds the Docker image, scans it, logs in to the registry, pushes it,
// and removes the local copy afterwards.
//
// Usage:
//   buildAndPushImage()
//   buildAndPushImage(image: 'org/app', tag: env.BUILD_NUMBER, credentialsId: 'DockerHub')
def call(Map config = [:]) {
    def image = config.image ?: env.DOCKER_IMAGE ?: 'mostafaosmanfathi/ivolve-nodejs-app'
    def tag = config.tag ?: env.BUILD_NUMBER ?: 'latest'
    def credentialsId = config.credentialsId ?: 'DockerHub'
    def scanSeverity = config.scanSeverity ?: 'HIGH,CRITICAL'
    def fullImage = "${image}:${tag}"

    echo "======== Build Docker Image ========"
    sh "docker build -t ${fullImage} ."

    echo "======== Scan Docker Image ========"
    sh "trivy image --exit-code 0 --severity ${scanSeverity} --no-progress ${fullImage}"

    echo "======== Docker Login & Push ========"
    try {
        withCredentials([
            usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'DOCKER_USERNAME',
                passwordVariable: 'DOCKER_PASSWORD'
            )
        ]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
            sh "docker push ${fullImage}"
        }
    } finally {
        sh 'docker logout || true'
    }

    echo "======== Delete image locally ========"
    sh "docker rmi ${fullImage} || true"
}
