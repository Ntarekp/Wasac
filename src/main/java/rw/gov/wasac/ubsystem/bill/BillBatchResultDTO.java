package rw.gov.wasac.ubsystem.bill;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BillBatchResultDTO {
    private int billingMonth;
    private int billingYear;
    private int generatedCount;
    private int skippedCount;
    private List<Bill> bills;
}
