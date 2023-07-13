FROM public.ecr.aws/sam/build-java11:latest as build-image

WORKDIR "/task"
COPY src/ src/
COPY pom.xml ./
RUN mvn -q clean install
RUN mvn dependency:copy-dependencies -DincludeScope=compile

#RUN yum install -y unzip && \
#    curl -Lo "/tmp/chromedriver.zip" "https://chromedriver.storage.googleapis.com/112.0.5615.49/chromedriver_linux64.zip" && \
#    curl -Lo "/tmp/chrome-linux.zip" "https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F1097615%2Fchrome-linux.zip?alt=media" && \
#    unzip /tmp/chromedriver.zip -d /opt/ && \
#    unzip /tmp/chrome-linux.zip -d /opt/
RUN yum install -y unzip && \
    curl -Lo "/tmp/chromedriver.zip" "https://chromedriver.storage.googleapis.com/2.37/chromedriver_linux64.zip" && \
    curl -Lo "/tmp/chrome.zip" "https://github.com/adieuadieu/serverless-chrome/releases/download/v1.0.0-41/stable-headless-chromium-amazonlinux-2017-03.zip" && \
    unzip /tmp/chromedriver.zip -d /opt/ && \
    unzip /tmp/chrome.zip -d /opt/
FROM public.ecr.aws/lambda/java:11
RUN yum install -y pango.x86_64 libXcomposite.x86_64 libXcursor.x86_64 libXdamage.x86_64 libXext.x86_64 libXi.x86_64 libXtst.x86_64 cups-libs.x86_64 libXScrnSaver.x86_64 libXrandr.x86_64 GConf2.x86_64 alsa-lib.x86_64 atk.x86_64 gtk3.x86_64 ipa-gothic-fonts xorg-x11-fonts-100dpi xorg-x11-fonts-75dpi xorg-x11-utils xorg-x11-fonts-cyrillic xorg-x11-fonts-Type1 xorg-x11-fonts-misc


COPY --from=build-image /task/target/classes /var/task/
COPY --from=build-image /task/target/dependency /var/task/lib
COPY --from=build-image /opt/headless-chromium /opt/chrome
COPY --from=build-image /opt/chromedriver /opt/
# Command can be overwritten by providing a different command in the template directly.
CMD ["com.kevin.adsprovider.StreamLambdaHandler::handleRequest"]