package krs.partner.service

import com.twitter.util.{ Future }
import krs.common.{ PartnerUtil }
import krs.thriftscala.{ PartnerService, PartnerResponse }

import krs.partner.api.{ PartnerApi }

object PartnerServiceImpl {
  def apply(api: PartnerApi) = {
    new PartnerService[Future] {
      def getOffers(creditScore: Int) = {
        api.getOffers(creditScore).map(offers => {
          val partnerOffers = offers.map(PartnerUtil.convertOffer)
          PartnerResponse(partnerOffers)
        })
      }
    }
  }
}
