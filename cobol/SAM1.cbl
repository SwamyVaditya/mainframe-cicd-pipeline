      * Program Name: SAM1                                             *
      * Description: Sample COBOL program for Mainframe CI/CD Pipeline *
      *================================================================*
       IDENTIFICATION DIVISION.
       PROGRAM-ID. SAM1.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       COPY SAMCOPY.
       PROCEDURE DIVISION.
       MAIN-LOGIC.
           DISPLAY 'STARTING MAIN-LOGIC FOR SAM1'.
           MOVE '00001' TO WS-ID.
           MOVE 'DEVOPS' TO WS-NAME.
           MOVE 'A' TO WS-STATUS.
           DISPLAY 'SAMPLE ID: ' WS-ID.
           DISPLAY 'SAMPLE NAME: ' WS-NAME.
           DISPLAY 'SAMPLE STATUS: ' WS-STATUS.
           GOBACK.