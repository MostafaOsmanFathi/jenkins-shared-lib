// Shared library step: deployToK8s
// Updates the image tag in the Kubernetes deployment manifest, configures
// kubectl, creates the image pull secret, applies the deployment and waits
// for the rollout to finish.
//
// Usage:
//   deployToK8s()
//   deployToK8s(image: 'org/app', tag: env.BUILD_NUMBER, namespace: 'ivolve')
def call(Map config = [:]) {
    def image = config.image ?: env.DOCKER_IMAGE ?: 'mostafaosmanfathi/ivolve-nodejs-app'
    def tag = config.tag ?: env.BUILD_NUMBER ?: 'latest'
    def fullImage = "${image}:${tag}"
    def deploymentFile = config.deploymentFile ?: 'k8s-project/deployment-nodjs.yaml'
    def namespace = config.namespace ?: 'ivolve'
    def deploymentName = config.deploymentName ?: 'ivolveapp'
    def server = config.server ?: 'https://192.168.122.54:6443'
    def k8sTokenCredentialsId = config.k8sTokenCredentialsId ?: 'k8s-jenkins-token'
    def dockerCredentialsId = config.dockerCredentialsId ?: 'DockerHub'

    echo "======== Update image in deployment file ========"
    sh "sed -i 's|${image}:.*|${fullImage}|' ${deploymentFile}"

    echo "======== Configure kubectl ========"
    withCredentials([
        string(
            credentialsId: k8sTokenCredentialsId,
            variable: 'K8S_TOKEN'
        )
    ]) {
        sh """
        kubectl config set-cluster k8s-cluster \\
        --server=${server} \\


        kubectl config set-credentials jenkins \\
        --token="\$K8S_TOKEN"

        kubectl config set-context jenkins \\
        --cluster=k8s-cluster \\
        --user=jenkins \\
        --namespace=${namespace}

        kubectl config use-context jenkins
        """
    }

    echo "======== Create image pull secret ========"
    withCredentials([
        usernamePassword(
            credentialsId: dockerCredentialsId,
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )
    ]) {
        sh """
        kubectl create secret docker-registry dockerhub-secret \\
          --docker-server=https://index.docker.io/v1/ \\
          --docker-username="\$DOCKER_USERNAME" \\
          --docker-password="\$DOCKER_PASSWORD" \\
          --namespace=${namespace} \\
          --dry-run=client -o yaml | kubectl apply -f -
        """
    }

    echo "======== Apply deployment ========"
    sh "kubectl apply -f ${deploymentFile}"

    echo "======== Rollout status ========"
    sh "kubectl rollout status deployment/${deploymentName} -n ${namespace} --timeout=600s"
}
