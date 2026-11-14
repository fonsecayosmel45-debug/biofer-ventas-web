"""Compile and test using H2 only; no API data loads into MySQL.
From the project root: py -3 backend/scripts/verificar_backend.py
"""
from pathlib import Path
import json
import os
import shutil
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


def main():
    backend = Path(__file__).resolve().parents[1]
    output = backend / ".local" / "verificacion"
    output.mkdir(parents=True, exist_ok=True)
    maven = shutil.which("mvn.cmd" if os.name == "nt" else "mvn")
    if not maven:
        maven = str(backend / ("mvnw.cmd" if os.name == "nt" else "mvnw"))
    if not Path(maven).exists():
        print("No se encontró Maven ni Maven Wrapper.")
        return 2
    log = output / "maven.log"
    started = time.time()
    print("Verificando en H2 temporal. No se conecta a MySQL.", flush=True)
    with log.open("w", encoding="utf-8") as file:
        try:
            result = subprocess.run([maven, "-B", "clean", "verify"], cwd=backend,
                                    stdout=file, stderr=subprocess.STDOUT, check=False)
        except KeyboardInterrupt:
            print("Verificación interrumpida.")
            return 130
    report = {"correcto": result.returncode == 0, "codigo_salida": result.returncode,
              "duracion_segundos": round(time.time() - started, 1), "base": "H2 temporal",
              "mysql_utilizado": False, "pruebas": [], "log": str(log)}
    for path in sorted((backend / "target/surefire-reports").glob("TEST-*.xml")):
        for case in ET.parse(path).getroot().findall("testcase"):
            failure = case.find("failure")
            if failure is None:
                failure = case.find("error")
            report["pruebas"].append({"clase": case.get("classname"), "prueba": case.get("name"),
                "estado": "ERROR" if failure is not None else "OMITIDA" if case.find("skipped") is not None else "OK",
                "detalle": failure.get("message", "") if failure is not None else ""})
    target = output / "resultado.json"
    target.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    for case in report["pruebas"]:
        print(f"{case['estado']}: {case['prueba']}")
    print(f"\nResultado: {target}\nLog completo: {log}")
    print("Verificación correcta; JAR en backend/target." if not result.returncode else "Falló la verificación. Revisa el log.")
    return result.returncode


if __name__ == "__main__":
    sys.exit(main())
