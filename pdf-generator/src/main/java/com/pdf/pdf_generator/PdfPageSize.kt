package com.pdf.pdf_generator



/**
 * This enum class represents different paper sizes that can be used in a PDF document.
 * Each paper size is defined as a value of the enum with its width and height specified as parameters.
 * The paper sizes included in this enum class are the ISO A, B, and C paper sizes as well as some popular American paper sizes like Letter, Legal, and Ledger.
 * The width and height values for each paper size are specified in pixels.
 *
 * @param width the width of the paper size in (1/72th of an inch).
 * @param height the height of the paper size in (1/72th of an inch).
 */
enum class PdfPageSize(val width: Int, val height: Int) {
    /** ISO A paper sizes */
    A0(width = 2384, height = 3370),
    A1(width = 1684, height = 2384),
    A2(width = 1190, height = 1684),
    A3(width = 842, height = 1190),
    A4(width = 595, height = 842),
    /** ISO B paper sizes */
    B0(width = 2835, height = 4008),
    B1(width = 2004, height = 2835),
    B2(width = 1417, height = 2004),
    B3(width = 1001, height = 1417),
    B4(width = 709, height = 1001),
    /** ISO C paper sizes */
    C0(width = 2599, height = 3676),
    C1(width = 1837, height = 2599),
    C2(width = 1297, height = 1837),
    C3(width = 918, height = 1297),
    C4(width = 649, height = 918),
    /** ISO American paper sizes */
    USLetter(width = 612, height = 792),
    USLegal(width = 612, height = 1008),
    USLedger(width = 1224, height = 792)
}