if [ "$1" == "-h" ] || [ "$1" == "help" ]; then
    echo "Invoke this with argugments {env} {docker_image_path:tag} "
    echo "eg: ./deployScript.sh sit gcr.io/crp-dev-dig-searchreplacement/searchfacade:a87a3f0c-1d4d-4d4b-979d-03846b172f41"
    exit 0
fi

profile="$1"
gcpImage="$2"

if [ -z "$profile" ] || [ -z "$gcpImage" ] 
then
	echo "Error! arguments are invalid or empty"
	echo "invoke this with argugments {env} {docker_image_path:tag} "
    echo "eg: ./deployScript.sh site gcr.io/crp-dev-dig-searchreplacement/searchfacade:a87a3f0c-1d4d-4d4b-979d-03846b172f41"
    exit 1
fi

oldImage=$(<lastestDeployedImage)
echo "****************"
echo "stopping image"
echo "****************"
sudo docker container stop compras-ingest
echo "****************"
echo "Removing container compras-ingest"
echo "****************"
sudo docker container rm -f compras-ingest
echo "****************"
echo "Removing image old image compras-ingest - "$oldImage
echo "****************"
sudo docker image rm -f $oldImage
echo "****************"
echo "pulling image compras-ingest from GCP - " $gcpImage
echo "****************"
sudo docker pull $gcpImage
echo "****************"
echo "Running compras-ingest image compras-ingest"
echo "****************"
sudo docker run -e "SPRING_PROFILES_ACTIVE="$profile --name compras-ingest -d -p 8443:8443 $gcpImage
sudo >lastestDeployedImage
sudo echo $gcpImage>lastestDeployedImage	
echo "compras-ingest image deployed"