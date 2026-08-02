//
//  KotlinStackTraceMapper.swift
//  CrashlyticsKMPSwift
//

import Foundation

/// Normaliza los frames de stack trace que llegan desde Kotlin/Native
/// (formato `kfun:dev.bonygod…#foo(){}`, líneas con prefijo `at `) a símbolos
/// legibles para `ExceptionModel.stackTrace`.
enum KotlinStackTraceMapper {

    /// Convierte una lista de frames en bruto (tal como los expone
    /// `Throwable.getStackTrace()` en Kotlin/Native) en símbolos legibles.
    static func mapFrames(_ rawFrames: [String]) -> [String] {
        rawFrames.map { normalize($0) }
    }

    /// Limpia un único frame:
    /// - quita el prefijo `at ` si está presente
    /// - extrae el símbolo `kfun:paquete.Clase#metodo(...){}` y lo deja como
    ///   `paquete.Clase.metodo`
    /// - si no reconoce el formato (p. ej. una línea "Caused by: …"), devuelve
    ///   el frame tal cual — mejor un frame poco simbolizado que perder la línea.
    static func normalize(_ rawFrame: String) -> String {
        var frame = rawFrame.trimmingCharacters(in: .whitespaces)

        if frame.hasPrefix("at ") {
            frame = String(frame.dropFirst(3)).trimmingCharacters(in: .whitespaces)
        }

        guard let kfunRange = frame.range(of: "kfun:") else {
            return frame
        }

        var symbol = String(frame[kfunRange.upperBound...])

        // "dev.bonygod…#foo(kotlin.String;){}" -> "dev.bonygod…#foo"
        if let parenIndex = symbol.firstIndex(of: "(") {
            symbol = String(symbol[..<parenIndex])
        }

        // "paquete.Clase#metodo" -> "paquete.Clase.metodo"
        symbol = symbol.replacingOccurrences(of: "#", with: ".")

        return symbol.isEmpty ? frame : symbol
    }
}
