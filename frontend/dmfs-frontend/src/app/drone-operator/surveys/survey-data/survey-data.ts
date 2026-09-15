import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import {
  OperatorService,
  Survey,
  SurveyData as SurveyDataRecord
} from '../../operator.service';

@Component({
  selector: 'app-survey-data',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    DatePipe
  ],
  templateUrl: './survey-data.html',
  styleUrl: './survey-data.scss'
})
export class SurveyData implements OnInit {

  private readonly api = inject(OperatorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  survey?: Survey;

loading = true;
error = '';

selectedFile?: File;

/* Data files already uploaded for this survey */


  fileName = '';
  fileSize = 0;
  rowCount = 0;

  headers: string[] = [];
  previewRows: string[][] = [];
  uploadedData: SurveyDataRecord[] = [];

  validationMessage = '';
  missingColumns: string[] = [];

  /* Upload state */
  uploading = false;
  uploadResult?: SurveyDataRecord;

  /*
   * Initial raw magnetometer fields we expect.
   *
   * We will make this configurable later when the
   * actual backend data model is finalized.
   */
  readonly requiredColumns = [
    'epoch_time_ns',
    'rm3100_x',
    'rm3100_y',
    'rm3100_z'
  ];

  ngOnInit(): void {

    const surveyId = Number(
      this.route.snapshot.paramMap.get('id')
    );

    if (!surveyId) {

      this.error = 'Invalid survey.';
      this.loading = false;

      this.cdr.detectChanges();

      return;
    }

    this.loadSurvey(surveyId);
  }
  get hasUploadedData(): boolean {
  return this.uploadedData.length > 0;
}


  private loadSurvey(id: number): void {

    this.loading = true;
    this.error = '';

    this.api.surveys().subscribe({

      next: surveys => {

        const survey = surveys.find(
          item => Number(item.id) === id
        );

        if (!survey) {

          this.error =
            'Survey not found or you do not have access to it.';

          this.loading = false;

          this.cdr.detectChanges();

          return;
        }

        this.survey = survey;

this.api.surveyData(id).subscribe({

  next: data => {

    this.uploadedData = data;

    this.loading = false;

    this.cdr.detectChanges();
  },

  error: error => {

    console.error(
      'LOAD SURVEY DATA FILES ERROR:',
      error
    );

    this.uploadedData = [];

    this.loading = false;

    this.cdr.detectChanges();
  }

});
      },

      error: error => {

        console.error(
          'LOAD SURVEY DATA ERROR:',
          error
        );

        this.error =
          error?.error?.message ||
          'Unable to load the survey.';

        this.loading = false;

        this.cdr.detectChanges();
      }

    });
  }


  get operatorName(): string {

    return 'Operator';
  }


  get initials(): string {

    const name = this.operatorName
      .split(' ')
      .filter(Boolean);

    return (
      `${name[0]?.[0] || 'O'}` +
      `${name[1]?.[0] || ''}`
    ).toUpperCase();
  }


  get statusLabel(): string {

    return this.survey?.status
      ? this.survey.status.replace(/_/g, ' ')
      : 'UNKNOWN';
  }


  get fileSizeLabel(): string {

    if (!this.fileSize) {
      return '0 KB';
    }

    if (this.fileSize < 1024) {
      return `${this.fileSize} B`;
    }

    if (this.fileSize < 1024 * 1024) {
      return `${(this.fileSize / 1024).toFixed(1)} KB`;
    }

    return `${(
      this.fileSize /
      (1024 * 1024)
    ).toFixed(2)} MB`;
  }


  onFileSelected(event: Event): void {

    const input =
      event.target as HTMLInputElement;

    const file =
      input.files?.[0];

    if (!file) {
      return;
    }

    this.resetFileState();

    this.selectedFile = file;
    this.fileName = file.name;
    this.fileSize = file.size;

    if (
      !file.name.toLowerCase().endsWith('.csv')
    ) {

      this.error =
        'For now, please select a CSV magnetometer file.';

      this.selectedFile = undefined;

      return;
    }

    this.readCsv(file);
  }


  private readCsv(file: File): void {

    const reader = new FileReader();

    reader.onload = () => {

      const text =
        String(reader.result || '');

      this.processCsv(text);

      this.cdr.detectChanges();
    };

    reader.onerror = () => {

      this.error =
        'The selected file could not be read.';

      this.cdr.detectChanges();
    };

    reader.readAsText(file);
  }


  private processCsv(csv: string): void {

    const lines =
      csv
        .split(/\r?\n/)
        .map(line => line.trim())
        .filter(Boolean);

    if (!lines.length) {

      this.error =
        'The selected CSV file is empty.';

      return;
    }

    this.headers =
      this.parseCsvLine(lines[0]);

    const dataLines =
      lines.slice(1);

    this.rowCount =
      dataLines.length;

    this.previewRows =
      dataLines
        .slice(0, 8)
        .map(line => this.parseCsvLine(line));

    this.validateHeaders();
  }


  private parseCsvLine(line: string): string[] {

    const result: string[] = [];

    let current = '';
    let insideQuotes = false;

    for (let i = 0; i < line.length; i++) {

      const char = line[i];

      if (char === '"') {

        insideQuotes = !insideQuotes;

      } else if (
        char === ',' &&
        !insideQuotes
      ) {

        result.push(current.trim());
        current = '';

      } else {

        current += char;
      }
    }

    result.push(current.trim());

    return result;
  }


  private validateHeaders(): void {

    const normalized =
      this.headers.map(
        header =>
          header.trim().toLowerCase()
      );

    this.missingColumns =
      this.requiredColumns.filter(
        required =>
          !normalized.includes(
            required.toLowerCase()
          )
      );

    if (!this.missingColumns.length) {

      this.validationMessage =
        'CSV structure looks valid for the initial magnetometer format.';

    } else {

      this.validationMessage =
        'Some expected magnetometer columns are missing.';
    }
  }


  get fileValid(): boolean {

    return !!this.selectedFile &&
      this.missingColumns.length === 0 &&
      this.rowCount > 0;
  }


  resetFile(): void {

    this.resetFileState();
  }


  private resetFileState(): void {

    this.selectedFile = undefined;

    this.fileName = '';
    this.fileSize = 0;
    this.rowCount = 0;

    this.headers = [];
    this.previewRows = [];

    this.validationMessage = '';
    this.missingColumns = [];

    this.error = '';
  }


  goBack(): void {

    if (this.survey) {

      this.router.navigate([
        '/drone-operator/surveys',
        this.survey.id
      ]);

      return;
    }

    this.router.navigate([
      '/drone-operator/surveys'
    ]);
  }


  logout(): void {

    /*
     * Logout will be wired to the shared AuthService
     * in the next shell consolidation.
     */
    this.router.navigate(['/login']);
  }


  uploadData(): void {

    if (
      !this.survey ||
      !this.selectedFile ||
      !this.fileValid ||
      this.uploading
    ) {
      return;
    }

    this.uploading = true;
    this.error = '';

    this.api
      .uploadSurveyData(
        this.survey.id,
        this.selectedFile
      )
      .subscribe({

       next: result => {

  console.log(
    'SURVEY DATA UPLOADED:',
    result
  );

  this.uploadResult = result;

  this.uploadedData = [
    result,
    ...this.uploadedData
  ];

  this.uploading = false;

  this.cdr.detectChanges();
},

        error: error => {

          console.error(
            'SURVEY DATA UPLOAD ERROR:',
            error
          );

          this.error =
            error?.error?.message ||
            'Unable to upload the survey data.';

          this.uploading = false;

          this.cdr.detectChanges();
        }

      });
  }
}