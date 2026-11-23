@echo off
REM ============================================================
REM  Zombie Survival — Script de lancement
REM  Auteur : Elmehdi Eddareiy
REM
REM  Prérequis :
REM    - Java 21+ dans le PATH  (ou JAVA_HOME défini)
REM    - Maven dans le PATH     (mvn)
REM
REM  Usage : double-cliquer sur ce fichier ou lancer depuis cmd
REM ============================================================

echo ============================================================
echo   ZOMBIE SURVIVAL - Lancement
echo ============================================================
echo.

REM Vérifier Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERREUR: Java non trouvé dans le PATH.
    echo Installez Java 21+ depuis https://adoptium.net
    pause
    exit /b 1
)

REM Vérifier Maven
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ERREUR: Maven non trouvé dans le PATH.
    echo Installez Maven depuis https://maven.apache.org/download.cgi
    echo Ou ouvrez le projet dans IntelliJ IDEA et lancez via la config Maven.
    pause
    exit /b 1
)

echo Compilation et lancement en cours...
echo.

mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ERREUR lors du lancement. Consultez les logs ci-dessus.
    pause
    exit /b 1
)
